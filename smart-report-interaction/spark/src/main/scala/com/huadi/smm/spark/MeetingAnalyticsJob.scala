package com.huadi.smm.spark

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.apache.spark.sql.{DataFrame, SparkSession}

object MeetingAnalyticsJob {

  val JDBC_URL = "jdbc:mysql://localhost:3306/smart_meeting?useSSL=false&characterEncoding=utf-8&serverTimezone=Asia/Shanghai"
  val JDBC_USER = "root"
  val JDBC_PASS = sys.env.getOrElse("DB_PASSWORD", "1234")
  val FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("MeetingAnalytics")
      .config("spark.sql.shuffle.partitions", "4")
      .getOrCreate()

    import spark.implicits._

    val now = LocalDateTime.now().format(FMT)

    // Read business tables
    val signinDF = readTable(spark, "sm_meeting_signin")
    val speechDF = readTable(spark, "sm_meeting_speech")
    val interactionDF = readTable(spark, "sm_meeting_interaction")
    val attendeeDF = readTable(spark, "meeting_attendee")
    val meetingDF = readTable(spark, "sm_meeting_info")
    val memberDF = readTable(spark, "sm_gm_members")

    // === Meeting Analytics ===
    val shouldAttendDF = attendeeDF.groupBy("meeting_id").count()
      .withColumnRenamed("count", "should_attend")
    val signedDF = signinDF.select("meeting_id", "user_id").distinct()
      .groupBy("meeting_id").count().withColumnRenamed("count", "actual_attend")
    val normalDF = signinDF.where("sign_status = 0").groupBy("meeting_id").count()
      .withColumnRenamed("count", "normal_count")
    val lateDF = signinDF.where("sign_status = 1").groupBy("meeting_id").count()
      .withColumnRenamed("count", "late_count")
    val speechCntDF = speechDF.groupBy("meeting_id").count()
      .withColumnRenamed("count", "speech_count")
    val interactionCntDF = interactionDF.groupBy("meeting_id").count()
      .withColumnRenamed("count", "interaction_count")

    val meetingAnalytics = meetingDF
      .join(shouldAttendDF, Seq("meeting_id"), "left")
      .join(signedDF, Seq("meeting_id"), "left")
      .join(normalDF, Seq("meeting_id"), "left")
      .join(lateDF, Seq("meeting_id"), "left")
      .join(speechCntDF, Seq("meeting_id"), "left")
      .join(interactionCntDF, Seq("meeting_id"), "left")
      .na.fill(0)
      .selectExpr(
        "meeting_id",
        "meeting_title",
        "meeting_date",
        "cast(coalesce(should_attend, 0) as int) as should_attend",
        "cast(coalesce(actual_attend, 0) as int) as actual_attend",
        "cast(round(coalesce(actual_attend, 0) / greatest(coalesce(should_attend, 1), 1) * 100, 2) as decimal(5,2)) as attend_rate",
        "cast(coalesce(normal_count, 0) as int) as normal_count",
        "cast(coalesce(late_count, 0) as int) as late_count",
        "cast(coalesce(speech_count, 0) as int) as speech_count",
        "cast(coalesce(interaction_count, 0) as int) as interaction_count",
        s"cast(round(coalesce(actual_attend, 0) / greatest(coalesce(should_attend, 1), 1) * 0.4 + coalesce(speech_count, 0) / greatest(coalesce(should_attend, 1), 1) * 100 * 0.3 + coalesce(interaction_count, 0) / greatest(coalesce(should_attend, 1), 1) * 100 * 0.3, 2) as decimal(5,2)) as quality_score",
        s"'$now' as created_at",
        s"'$now' as updated_at"
      )
      .select("meeting_id", "meeting_title", "meeting_date", "should_attend", "actual_attend",
        "attend_rate", "normal_count", "late_count", "speech_count", "interaction_count",
        "quality_score", "created_at", "updated_at")

    writeTable(meetingAnalytics, "sm_meeting_analytics")

    // === Department Analytics ===
    val attendeeSigned = attendeeDF
      .join(signinDF.select("meeting_id", "user_id").distinct(), Seq("meeting_id", "user_id"), "left_outer")
      .join(memberDF.selectExpr("user_id", "department"), Seq("user_id"), "left")

    val deptMeetingCnt = attendeeSigned.select("department", "meeting_id").distinct()
      .groupBy("department").count().withColumnRenamed("count", "meeting_count")

    val deptRates = attendeeSigned.groupBy("department")
      .agg(
        org.apache.spark.sql.functions.count("*").as("total"),
        org.apache.spark.sql.functions.sum(
          org.apache.spark.sql.functions.when(
            attendeeSigned("meeting_id").isNotNull.and(signinDF("user_id").isNotNull), 1).otherwise(0)
        ).as("signed")
      )
      .selectExpr("department",
        "cast(round(signed / greatest(total, 1) * 100, 2) as decimal(5,2)) as avg_attend_rate")

    val speechWithDept = speechDF
      .join(memberDF, speechDF("speaker_id") === memberDF("user_id"), "left")
      .groupBy("department").count().withColumnRenamed("count", "total_speech_count")

    val interactionWithDept = interactionDF
      .join(memberDF, interactionDF("from_user_id") === memberDF("user_id"), "left")
      .groupBy("department").count().withColumnRenamed("count", "total_interaction_count")

    val deptAnalytics = deptMeetingCnt
      .join(deptRates, Seq("department"), "left")
      .join(speechWithDept, Seq("department"), "left")
      .join(interactionWithDept, Seq("department"), "left")
      .na.fill(0)
      .selectExpr(
        "department",
        "cast(coalesce(meeting_count, 0) as int) as meeting_count",
        "coalesce(avg_attend_rate, 0) as avg_attend_rate",
        "cast(coalesce(total_speech_count, 0) as int) as total_speech_count",
        "cast(coalesce(total_interaction_count, 0) as int) as total_interaction_count",
        s"'$now' as updated_at"
      )

    writeTable(deptAnalytics, "sm_department_analytics")

    // === Member Analytics ===
    val memberMeetings = attendeeDF.groupBy("user_id").count()
      .withColumnRenamed("count", "total_meetings")
    val memberSigned = signinDF.select("meeting_id", "user_id").distinct()
      .groupBy("user_id").count().withColumnRenamed("count", "attended_count")
    val memberNormal = signinDF.where("sign_status = 0").groupBy("user_id").count()
      .withColumnRenamed("count", "normal_count")
    val memberLate = signinDF.where("sign_status = 1").groupBy("user_id").count()
      .withColumnRenamed("count", "late_count")
    val memberSpeech = speechDF.groupBy("speaker_id").count()
      .withColumnRenamed("speaker_id", "spk_id").withColumnRenamed("count", "speech_count")
    val memberInteraction = interactionDF.groupBy("from_user_id").count()
      .withColumnRenamed("from_user_id", "int_id").withColumnRenamed("count", "interaction_count")

    val memberAnalytics = memberDF
      .selectExpr("user_id", "name as user_name", "department")
      .join(memberMeetings, Seq("user_id"), "left")
      .join(memberSigned, Seq("user_id"), "left")
      .join(memberNormal, Seq("user_id"), "left")
      .join(memberLate, Seq("user_id"), "left")
      .join(memberSpeech, memberDF("user_id") === memberSpeech("spk_id"), "left")
      .join(memberInteraction, memberDF("user_id") === memberInteraction("int_id"), "left")
      .na.fill(0)
      .selectExpr(
        "user_id",
        "user_name",
        "department",
        "cast(coalesce(total_meetings, 0) as int) as total_meetings",
        "cast(coalesce(attended_count, 0) as int) as attended_count",
        "cast(round(coalesce(attended_count, 0) / greatest(coalesce(total_meetings, 1), 1) * 100, 2) as decimal(5,2)) as attend_rate",
        "cast(coalesce(normal_count, 0) as int) as normal_count",
        "cast(coalesce(late_count, 0) as int) as late_count",
        "cast(coalesce(speech_count, 0) as int) as speech_count",
        "cast(coalesce(interaction_count, 0) as int) as interaction_count",
        s"'$now' as last_updated"
      )

    writeTable(memberAnalytics, "sm_member_analytics")

    spark.stop()
  }

  def readTable(spark: SparkSession, table: String): DataFrame = {
    spark.read.format("jdbc")
      .option("url", JDBC_URL)
      .option("dbtable", table)
      .option("user", JDBC_USER)
      .option("password", JDBC_PASS)
      .load()
  }

  def writeTable(df: DataFrame, table: String): Unit = {
    df.write.format("jdbc")
      .option("url", JDBC_URL)
      .option("dbtable", table)
      .option("user", JDBC_USER)
      .option("password", JDBC_PASS)
      .mode("overwrite")
      .save()
  }
}
