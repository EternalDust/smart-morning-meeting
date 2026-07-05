package com.huadi.smm.spark

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._

object MeetingAnalyticsJob {

  val JDBC_URL = "jdbc:mysql://localhost:3307/smart_meeting?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai"
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

    val signinDF = readTable(spark, "sm_meeting_signin")
    val speechDF = readTable(spark, "sm_meeting_speech")
    val interactionDF = readTable(spark, "sm_meeting_interaction")
    val attendeeDF = readTable(spark, "sm_meeting_attendee")
    val meetingDF = readTable(spark, "sm_meeting_info").withColumnRenamed("id", "meeting_id")
    val memberDF = readTable(spark, "sm_gm_members")

    println(s"signin=${signinDF.count()} speech=${speechDF.count()} interaction=${interactionDF.count()} attendee=${attendeeDF.count()} meeting=${meetingDF.count()} member=${memberDF.count()}")

    // === Meeting Analytics === (all equi-joins on meeting_id)
    val shouldAttendDF = attendeeDF.groupBy("meeting_id").count().withColumnRenamed("count", "should_attend")
    val signedDF = signinDF.select("meeting_id", "user_id").distinct().groupBy("meeting_id").count().withColumnRenamed("count", "actual_attend")
    val normalDF = signinDF.where("sign_status = 0").groupBy("meeting_id").count().withColumnRenamed("count", "normal_count")
    val lateDF = signinDF.where("sign_status = 1").groupBy("meeting_id").count().withColumnRenamed("count", "late_count")
    val speechCntDF = speechDF.groupBy("meeting_id").count().withColumnRenamed("count", "speech_count")
    val interactionCntDF = interactionDF.groupBy("meeting_id").count().withColumnRenamed("count", "interaction_count")

    val meetingAnalytics = meetingDF
      .join(shouldAttendDF, Seq("meeting_id"), "left")
      .join(signedDF, Seq("meeting_id"), "left")
      .join(normalDF, Seq("meeting_id"), "left")
      .join(lateDF, Seq("meeting_id"), "left")
      .join(speechCntDF, Seq("meeting_id"), "left")
      .join(interactionCntDF, Seq("meeting_id"), "left")
      .na.fill(0)
      .select(
        col("meeting_id"),
        col("title").as("meeting_title"),
        col("start_time").cast("string").as("meeting_date"),
        col("should_attend").cast("int"),
        col("actual_attend").cast("int"),
        round(col("actual_attend") / greatest(col("should_attend"), lit(1)) * 100, 2).as("attend_rate"),
        col("normal_count").cast("int"),
        col("late_count").cast("int"),
        col("speech_count").cast("int"),
        col("interaction_count").cast("int"),
        round(col("actual_attend") / greatest(col("should_attend"), lit(1)) * 0.4 +
              col("speech_count") / greatest(col("should_attend"), lit(1)) * 100 * 0.3 +
              col("interaction_count") / greatest(col("should_attend"), lit(1)) * 100 * 0.3, 2).as("quality_score"),
        lit(now).as("created_at"),
        lit(now).as("updated_at")
      )
    writeTable(meetingAnalytics, "sm_meeting_analytics")
    println("Meeting analytics written")

    // === Department Analytics ===
    val signedMark = signinDF.select("meeting_id", "user_id").distinct().withColumn("is_signed", lit(1))
    val deptBase = attendeeDF
      .join(signedMark, Seq("meeting_id", "user_id"), "left_outer")
      .join(memberDF.select(col("user_id"), col("dept").as("department")), Seq("user_id"), "left")
      .na.fill(0, Seq("is_signed"))

    val deptMeetingCnt = deptBase.select("department", "meeting_id").distinct()
      .groupBy("department").count().withColumnRenamed("count", "meeting_count")

    val deptRates = deptBase.groupBy("department")
      .agg(count("*").as("total"), sum(col("is_signed")).as("signed"))
      .withColumn("avg_attend_rate", round(col("signed") / greatest(col("total"), lit(1)) * 100, 2))
      .select("department", "avg_attend_rate")

    val speechDept = speechDF.join(memberDF, speechDF("speaker_id") === memberDF("user_id"), "left")
      .groupBy("dept").count().withColumnRenamed("count", "total_speech_count").withColumnRenamed("dept", "department")
    val interDept = interactionDF.join(memberDF, Seq("user_id"), "left")
      .groupBy("dept").count().withColumnRenamed("count", "total_interaction_count").withColumnRenamed("dept", "department")

    val deptAnalytics = deptMeetingCnt
      .join(deptRates, Seq("department"), "left")
      .join(speechDept, Seq("department"), "left")
      .join(interDept, Seq("department"), "left")
      .na.fill(0)
      .select(
        col("department"),
        col("meeting_count").cast("int"),
        col("avg_attend_rate"),
        col("total_speech_count").cast("int"),
        col("total_interaction_count").cast("int"),
        lit(now).as("updated_at")
      )
    writeTable(deptAnalytics, "sm_department_analytics")
    println("Department analytics written")

    // === Member Analytics === (all equi-joins on user_id)
    val memMeetings = attendeeDF.groupBy("user_id").count().withColumnRenamed("count", "total_meetings")
    val memSigned = signinDF.select("meeting_id", "user_id").distinct().groupBy("user_id").count().withColumnRenamed("count", "attended_count")
    val memNormal = signinDF.where("sign_status = 0").groupBy("user_id").count().withColumnRenamed("count", "normal_count")
    val memLate = signinDF.where("sign_status = 1").groupBy("user_id").count().withColumnRenamed("count", "late_count")
    val memSpeech = speechDF.groupBy("speaker_id").count().withColumnRenamed("speaker_id", "user_id").withColumnRenamed("count", "speech_count")
    val memInter = interactionDF.groupBy("user_id").count().withColumnRenamed("count", "interaction_count")

    val memberAnalytics = memberDF
      .select(col("user_id"), col("name").as("user_name"), col("dept").as("department"))
      .join(memMeetings, Seq("user_id"), "left")
      .join(memSigned, Seq("user_id"), "left")
      .join(memNormal, Seq("user_id"), "left")
      .join(memLate, Seq("user_id"), "left")
      .join(memSpeech, Seq("user_id"), "left")
      .join(memInter, Seq("user_id"), "left")
      .na.fill(0)
      .select(
        col("user_id"),
        col("user_name"),
        col("department"),
        col("total_meetings").cast("int"),
        col("attended_count").cast("int"),
        round(col("attended_count") / greatest(col("total_meetings"), lit(1)) * 100, 2).as("attend_rate"),
        col("normal_count").cast("int"),
        col("late_count").cast("int"),
        col("speech_count").cast("int"),
        col("interaction_count").cast("int"),
        lit(now).as("last_updated")
      )
    writeTable(memberAnalytics, "sm_member_analytics")
    println("Member analytics written")

    println("All analytics completed successfully")
    spark.stop()
  }

  def readTable(spark: SparkSession, table: String): DataFrame = {
    spark.read.format("jdbc")
      .option("url", JDBC_URL)
      .option("dbtable", table)
      .option("user", JDBC_USER)
      .option("password", JDBC_PASS)
      .option("driver", "com.mysql.cj.jdbc.Driver")
      .load()
  }

  def writeTable(df: DataFrame, table: String): Unit = {
    df.write.format("jdbc")
      .option("url", JDBC_URL)
      .option("dbtable", table)
      .option("user", JDBC_USER)
      .option("password", JDBC_PASS)
      .option("driver", "com.mysql.cj.jdbc.Driver")
      .option("truncate", "true")
      .mode("overwrite")
      .save()
  }
}
