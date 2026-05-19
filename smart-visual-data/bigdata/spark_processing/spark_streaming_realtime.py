import json
import redis
from pyspark.sql import SparkSession
from pyspark.sql.functions import col, from_json, sum as _sum
from pyspark.sql.types import StructType, StringType, IntegerType

# Redis 连接配置
redis_client = redis.Redis(host='localhost', port=6379, db=0, decode_responses=True)

def process_batch(df, epoch_id):
    """
    处理每个微批次的数据，计算参会率和问题解决率，并将结果存入 Redis 供后端和前端调用。
    """
    # 计算全体汇总指标
    metrics_df = df.agg(
        _sum("expected_attendees").alias("total_expected"),
        _sum("actual_attendees").alias("total_actual"),
        _sum("total_issues").alias("all_issues"),
        _sum("resolved_issues").alias("all_resolved")
    ).collect()

    if metrics_df and len(metrics_df) > 0 and metrics_df[0]["total_expected"] is not None:
        row = metrics_df[0]
        attendance_rate = (row["total_actual"] / row["total_expected"]) * 100 if row["total_expected"] > 0 else 0
        resolution_rate = (row["all_resolved"] / row["all_issues"]) * 100 if row["all_issues"] > 0 else 0
        
        # 封装到 JSON 中放入 Redis
        result = {
            "attendanceRate": round(attendance_rate, 2),
            "resolutionRate": round(resolution_rate, 2),
            "riskLevel": "NORMAL" if attendance_rate > 85 else "WARNING" # 简单阈值预警示例
        }
        
        # 将结果写入 Redis 缓存。后端 Spring Boot 会定时读取此 Key。
        redis_client.set("spark:metrics:morning_meeting:realtime", json.dumps(result))
        print(f"[{epoch_id}] 已更新 Redis: {result}")

if __name__ == "__main__":
    # 初始化 Spark Session
    spark = SparkSession.builder \
        .appName("MorningMeetingRealTimeAnalysis") \
        .master("local[*]") \
        .config("spark.jars.packages", "org.apache.spark:spark-sql-kafka-0-10_2.12:3.5.1") \
        .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    # 定义数据约束(Schema)
    schema = StructType() \
        .add("timestamp", IntegerType()) \
        .add("department", StringType()) \
        .add("expected_attendees", IntegerType()) \
        .add("actual_attendees", IntegerType()) \
        .add("total_issues", IntegerType()) \
        .add("resolved_issues", IntegerType())

    print("开始监听 Kafka topic: morning_meeting_topic ...")

    # 连接到 Kafka 读取流数据
    df = spark \
        .readStream \
        .format("kafka") \
        .option("kafka.bootstrap.servers", "localhost:9092") \
        .option("subscribe", "morning_meeting_topic") \
        .load()

    # 解析 JSON 数据
    parsed_df = df.select(from_json(col("value").cast("string"), schema).alias("data")).select("data.*")

    # 使用 foreachBatch 将微批次写入 Redis
    query = parsed_df.writeStream \
        .outputMode("update") \
        .foreachBatch(process_batch) \
        .start()

    query.awaitTermination()
