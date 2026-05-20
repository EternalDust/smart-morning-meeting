"""
Spark Streaming 实时处理 + 异常检测集成
功能：
  1. 从 Kafka 消费晨会数据
  2. 计算参会率、问题解决率等核心指标 → 写入 Redis（供后端展示）
  3. 调用 IF + LSTM 异常检测管道 → 预警写入 MySQL + Redis（供后端预警展示）
"""
import json
import sys
import os

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

import redis
from pyspark.sql import SparkSession
from pyspark.sql.functions import col, from_json, sum as _sum
from pyspark.sql.types import StructType, StringType, IntegerType

from inference.realtime_detector import RealtimeDetector

# Redis 连接配置
redis_client = redis.Redis(host="localhost", port=6379, db=0, decode_responses=True)

# ── 全局初始化检测器（微批次间复用） ──
_detector: RealtimeDetector = None


def get_detector() -> RealtimeDetector:
    global _detector
    if _detector is None:
        _detector = RealtimeDetector()
        _detector.load_models()
        if _detector.is_ready:
            print("[AnomalyDetection] 异常检测模型加载成功，已启用实时检测")
        else:
            print("[AnomalyDetection] 模型未就绪，实时检测跳过（请先运行训练脚本）")
    return _detector


def process_batch(df, epoch_id):
    """
    处理每个微批次的数据：
    1. 计算汇总指标 → Redis（原逻辑）
    2. 逐条执行异常检测 → MySQL bi_warn_record + Redis（新增逻辑）
    """
    # ── 原有逻辑：汇总指标写入 Redis ──
    metrics_df = df.agg(
        _sum("expected_attendees").alias("total_expected"),
        _sum("actual_attendees").alias("total_actual"),
        _sum("total_issues").alias("all_issues"),
        _sum("resolved_issues").alias("all_resolved"),
    ).collect()

    if metrics_df and len(metrics_df) > 0 and metrics_df[0]["total_expected"] is not None:
        row = metrics_df[0]
        attendance_rate = (row["total_actual"] / row["total_expected"]) * 100 if row["total_expected"] > 0 else 0
        resolution_rate = (row["all_resolved"] / row["all_issues"]) * 100 if row["all_issues"] > 0 else 0

        result = {
            "attendanceRate": round(attendance_rate, 2),
            "resolutionRate": round(resolution_rate, 2),
            "riskLevel": "NORMAL" if attendance_rate > 85 else "WARNING",
        }
        redis_client.set("spark:metrics:morning_meeting:realtime", json.dumps(result))
        print(f"[{epoch_id}] 已更新 Redis 指标: {result}")

    # ── 新增逻辑：逐条异常检测 ──
    detector = get_detector()
    if detector.is_ready:
        rows = df.collect()
        for row in rows:
            raw_data = {
                "department": row.get("department", ""),
                "dept_id": row.get("dept_id", 0),
                "attend_rate": float(
                    (row["actual_attendees"] / row["expected_attendees"] * 100)
                    if row["expected_attendees"] and row["expected_attendees"] > 0
                    else 0
                ),
                "solve_rate": float(
                    (row["resolved_issues"] / row["total_issues"] * 100)
                    if row["total_issues"] and row["total_issues"] > 0
                    else 0
                ),
                "late_num": float(row.get("late_num", 0) or 0),
                "absent_num": float(row["expected_attendees"] - row["actual_attendees"])
                if row.get("expected_attendees") and row.get("actual_attendees")
                else 0,
                "overdue_count": float(row.get("overdue_count", 0) or 0),
                "urgent_count": float(row.get("urgent_count", 0) or 0),
                "avg_duration": float(row.get("avg_duration", 15) or 15),
            }
            warn_record = detector.detect(raw_data)
            if warn_record:
                print(f"[{epoch_id}] 异常预警: {warn_record['detail']}")
    else:
        print(f"[{epoch_id}] 异常检测模型未就绪，跳过检测")


if __name__ == "__main__":
    spark = SparkSession.builder \
        .appName("MorningMeetingRealTimeAnalysis") \
        .master("local[*]") \
        .config("spark.jars.packages", "org.apache.spark:spark-sql-kafka-0-10_2.12:3.5.1") \
        .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    schema = StructType() \
        .add("timestamp", IntegerType()) \
        .add("department", StringType()) \
        .add("dept_id", IntegerType()) \
        .add("expected_attendees", IntegerType()) \
        .add("actual_attendees", IntegerType()) \
        .add("total_issues", IntegerType()) \
        .add("resolved_issues", IntegerType()) \
        .add("late_num", IntegerType()) \
        .add("overdue_count", IntegerType()) \
        .add("urgent_count", IntegerType()) \
        .add("avg_duration", IntegerType())

    print("开始监听 Kafka topic: morning_meeting_topic ...")

    df = spark \
        .readStream \
        .format("kafka") \
        .option("kafka.bootstrap.servers", "localhost:9092") \
        .option("subscribe", "morning_meeting_topic") \
        .load()

    parsed_df = df.select(from_json(col("value").cast("string"), schema).alias("data")).select("data.*")

    query = parsed_df.writeStream \
        .outputMode("update") \
        .foreachBatch(process_batch) \
        .start()

    query.awaitTermination()
