package com.huadi.smm.spark

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._

object MeetingAnalyticsJob {

  val JDBC_URL = "jdbc:mysql://centos4:3307/smart_meeting?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai"
  val JDBC_USER = "root"
  val JDBC_PASS = sys.env.getOrElse("DB_PASSWORD", "1234")
  val FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("MeetingAnalyticsV2")
      .config("spark.sql.shuffle.partitions", "8")
      .getOrCreate()

    import spark.implicits._

    val now = LocalDateTime.now().format(FMT)

    val signinDF = readTable(spark, "sm_meeting_signin")
    val speechDF = readTable(spark, "sm_meeting_speech")
    val interactionDF = readTable(spark, "sm_meeting_interaction")
    val attendeeDF = readTable(spark, "sm_meeting_attendee")
    val meetingDF = readTable(spark, "sm_meeting_info")
    val memberDF = readTable(spark, "sm_gm_members")

    println(s"meetings=${meetingDF.count()} signins=${signinDF.count()} speeches=${speechDF.count()} interactions=${interactionDF.count()}")

    // rename meeting id column
    val mdf = meetingDF.withColumnRenamed("id", "meeting_id")

    // === 1. Meeting Analytics (per-meeting stats + anomaly flag) ===
    val shouldAttendDF = attendeeDF.groupBy("meeting_id").count().withColumnRenamed("count", "should_attend")
    val signedDF = signinDF.select("meeting_id", "user_id").distinct().groupBy("meeting_id").count().withColumnRenamed("count", "actual_attend")
    val normalDF = signinDF.where("sign_status = 0").groupBy("meeting_id").count().withColumnRenamed("count", "normal_count")
    val lateDF = signinDF.where("sign_status = 1").groupBy("meeting_id").count().withColumnRenamed("count", "late_count")
    val speechCntDF = speechDF.groupBy("meeting_id").count().withColumnRenamed("count", "speech_count")
    val interactionCntDF = interactionDF.groupBy("meeting_id").count().withColumnRenamed("count", "interaction_count")

    var baseDF = mdf
      .join(shouldAttendDF, Seq("meeting_id"), "left")
      .join(signedDF, Seq("meeting_id"), "left")
      .join(normalDF, Seq("meeting_id"), "left")
      .join(lateDF, Seq("meeting_id"), "left")
      .join(speechCntDF, Seq("meeting_id"), "left")
      .join(interactionCntDF, Seq("meeting_id"), "left")
      .na.fill(0)

    baseDF = baseDF.select(
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

    // Add week/month for trend aggregation
    val withPeriod = baseDF
      .withColumn("meeting_week", date_format(to_timestamp(col("meeting_date"), "yyyy-MM-dd HH:mm:ss"), "yyyy-ww"))
      .withColumn("meeting_month", date_format(to_timestamp(col("meeting_date"), "yyyy-MM-dd HH:mm:ss"), "yyyy-MM"))

    // === Anomaly detection using Z-score ===
    val stats = baseDF.agg(
      mean("attend_rate").as("avg_rate"), stddev("attend_rate").as("std_rate"),
      mean("quality_score").as("avg_score"), stddev("quality_score").as("std_score")
    ).collect()(0)
    val (avgRate, stdRate, avgScore, stdScore) =
      (stats.getAs[Double]("avg_rate"), math.max(stats.getAs[Double]("std_rate"), 0.01),
       stats.getAs[Double]("avg_score"), math.max(stats.getAs[Double]("std_score"), 0.01))

    // Broadcast stats for anomaly detection
    val meetingWithAnomaly = baseDF.withColumn("is_anomaly",
      when(abs(col("attend_rate") - lit(avgRate)) > lit(2 * stdRate), lit(1)).otherwise(lit(0))
    )

    writeTable(meetingWithAnomaly.withColumn("created_at", lit(now)).withColumn("updated_at", lit(now)), "sm_meeting_analytics")
    println("Meeting analytics with anomaly detection written")

    // === 2. Weekly Trend Analytics ===
    val weeklyTrend = withPeriod.groupBy("meeting_week")
      .agg(
        round(mean("attend_rate"), 2).as("avg_attend_rate"),
        round(mean("quality_score"), 2).as("avg_quality_score"),
        sum("speech_count").as("total_speeches"),
        sum("interaction_count").as("total_interactions"),
        count("meeting_id").as("meeting_count")
      )
      .orderBy("meeting_week")

    writeTable(weeklyTrend.withColumn("created_at", lit(now)), "sm_meeting_analytics_weekly")
    println("Weekly trend written")

    // === 3. Department Analytics with Z-score anomaly ===
    val attendeeSigned = attendeeDF
      .join(signinDF.select("meeting_id", "user_id").distinct().withColumn("is_signed", lit(1)),
        Seq("meeting_id", "user_id"), "left_outer")
      .join(memberDF.select(col("user_id"), col("dept").as("department")), Seq("user_id"), "left")
      .na.fill(0, Seq("is_signed"))

    val deptBase = attendeeSigned.groupBy("department")
      .agg(
        count("*").as("total_slots"),
        sum(col("is_signed")).as("total_signed"),
        countDistinct("meeting_id").as("meeting_count")
      )
      .withColumn("avg_attend_rate", round(col("total_signed") / greatest(col("total_slots"), lit(1)) * 100, 2))

    // department Z-score
    val deptStats = deptBase.agg(mean("avg_attend_rate").as("d_avg"), stddev("avg_attend_rate").as("d_std")).collect()(0)
    val (dAvg, dStd) = (deptStats.getAs[Double]("d_avg"), math.max(deptStats.getAs[Double]("d_std"), 0.01))

    val speechDept = speechDF.join(memberDF, speechDF("speaker_id") === memberDF("user_id"), "left")
      .groupBy("dept").count().withColumnRenamed("count", "total_speech_count").withColumnRenamed("dept", "department")
    val interDept = interactionDF.join(memberDF, Seq("user_id"), "left")
      .groupBy("dept").count().withColumnRenamed("count", "total_interaction_count").withColumnRenamed("dept", "department")

    var deptResult = deptBase
      .join(speechDept, Seq("department"), "left")
      .join(interDept, Seq("department"), "left")
      .na.fill(0)

    deptResult = deptResult.select(
      col("department"),
      col("meeting_count").cast("int"),
      col("avg_attend_rate"),
      col("total_speech_count").cast("int"),
      col("total_interaction_count").cast("int"),
      when(abs(col("avg_attend_rate") - lit(dAvg)) > lit(2 * dStd), lit(1)).otherwise(lit(0)).as("is_anomaly"),
      lit(now).as("updated_at")
    )

    writeTable(deptResult, "sm_department_analytics")
    println("Department analytics with anomaly detection written")

    // === 4. Morning vs Evening comparison ===
    val timePattern = signinDF
      .withColumn("sign_hour", hour(to_timestamp(col("sign_time"), "yyyy-MM-dd HH:mm:ss")))
      .withColumn("period", when(col("sign_hour") < 12, lit("早班")).otherwise(lit("晚班")))
      .groupBy("period", "sign_status")
      .agg(count("*").as("cnt"))
      .groupBy("period")
      .pivot("sign_status", Seq("0", "1"))
      .agg(sum("cnt"))
      .na.fill(0)
      .withColumnRenamed("0", "normal_count")
      .withColumnRenamed("1", "late_count")
      .withColumn("total", col("normal_count") + col("late_count"))
      .withColumn("punctual_rate", round(col("normal_count") / greatest(col("total"), lit(1)) * 100, 2))

    writeTable(timePattern.withColumn("updated_at", lit(now)), "sm_time_analytics")
    println("Time pattern analytics written")

    // === 5. Member Analytics ===
    val memMeetings = attendeeDF.groupBy("user_id").count().withColumnRenamed("count", "total_meetings")
    val memSigned = signinDF.select("meeting_id", "user_id").distinct().groupBy("user_id").count().withColumnRenamed("count", "attended_count")
    val memNormal = signinDF.where("sign_status = 0").groupBy("user_id").count().withColumnRenamed("count", "normal_count")
    val memLate = signinDF.where("sign_status = 1").groupBy("user_id").count().withColumnRenamed("count", "late_count")
    val memSpeech = speechDF.groupBy("speaker_id").count().withColumnRenamed("speaker_id", "user_id").withColumnRenamed("count", "speech_count")
    val memInter = interactionDF.groupBy("user_id").count().withColumnRenamed("count", "interaction_count")

    var memResult = memberDF
      .select(col("user_id"), col("name").as("user_name"), col("dept").as("department"))
      .join(memMeetings, Seq("user_id"), "left")
      .join(memSigned, Seq("user_id"), "left")
      .join(memNormal, Seq("user_id"), "left")
      .join(memLate, Seq("user_id"), "left")
      .join(memSpeech, Seq("user_id"), "left")
      .join(memInter, Seq("user_id"), "left")
      .na.fill(0)

    memResult = memResult.select(
      col("user_id"), col("user_name"), col("department"),
      col("total_meetings").cast("int"), col("attended_count").cast("int"),
      round(col("attended_count") / greatest(col("total_meetings"), lit(1)) * 100, 2).as("attend_rate"),
      col("normal_count").cast("int"), col("late_count").cast("int"),
      col("speech_count").cast("int"), col("interaction_count").cast("int"),
      lit(now).as("last_updated")
    )

    writeTable(memResult, "sm_member_analytics")
    println("Member analytics written")

    println("All analytics completed!")
    spark.stop()
  }

  def readTable(spark: SparkSession, table: String): DataFrame = {
    spark.read.format("jdbc")
      .option("url", JDBC_URL).option("dbtable", table)
      .option("user", JDBC_USER).option("password", JDBC_PASS)
      .option("driver", "com.mysql.cj.jdbc.Driver").load()
  }

  def writeTable(df: DataFrame, table: String): Unit = {
    df.write.format("jdbc")
      .option("url", JDBC_URL).option("dbtable", table)
      .option("user", JDBC_USER).option("password", JDBC_PASS)
      .option("driver", "com.mysql.cj.jdbc.Driver")
      .option("truncate", "true").mode("overwrite").save()
  }
}
