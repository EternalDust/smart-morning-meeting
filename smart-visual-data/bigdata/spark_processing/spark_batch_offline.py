import os
import sys

from pyspark.sql import SparkSession
from pyspark.sql.functions import col, mean, when

# 让脚本可以直接 import models.config（与 requirements.txt 一致，密码走环境变量）
sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))
from models.config import MYSQL_HOST, MYSQL_PORT, MYSQL_DB, MYSQL_USER, MYSQL_PASSWORD

CLEAN_DATA_TABLE = "data_clean_data"
BI_MEDICAL_TABLE = "bi_stat_medical"


def _mysql_conn():
    """返回共享库 smart_meeting 的 pymysql 连接；失败返回 None（演示环境不强制）。"""
    try:
        import pymysql
        return pymysql.connect(
            host=MYSQL_HOST, port=MYSQL_PORT, database=MYSQL_DB,
            user=MYSQL_USER, password=MYSQL_PASSWORD, charset="utf8mb4",
            cursorclass=pymysql.cursors.DictCursor,
        )
    except Exception as e:  # noqa: BLE001 - 离线任务容忍 DB 不可用
        print(f"[WARN] 连接 MySQL 失败({e})，将使用演示数据兜底")
        return None


def load_clean_data(conn):
    """读取清洗后数据表；DB 不可用时生成与演示数据同构的兜底数据。"""
    if conn is None:
        return _demo_clean_data()
    try:
        with conn.cursor() as cur:
            cur.execute(
                "SELECT visit_time, department, quality_score, age FROM %s"
                % CLEAN_DATA_TABLE
            )
            return cur.fetchall()
    except Exception as e:  # noqa: BLE001
        print(f"[WARN] 读取 {CLEAN_DATA_TABLE} 失败({e})，使用演示数据兜底")
        return _demo_clean_data()


def _demo_clean_data():
    """兜底数据：与 sql/init.sql 演示种子同日期范围（2026-06 工作日）。"""
    return [
        {"visit_time": f"2026-06-{d:02d} 09:00:00", "department": dept,
         "quality_score": 80 + ((d * 7 + len(dept)) % 19), "age": 40 + (d % 30)}
        for d in range(1, 20)
        for dept in ["外科", "内科", "儿科", "妇产科", "骨科", "急诊科", "影像科"]
    ]


def aggregate_clean_data(spark, rows):
    """
    基于 data_clean_data 的离线指标聚合：
    - 缺失质量分：按日期填充该日均值
    - 异常质量分：剔除超出 [0,100] 或 3σ 区间的记录
    - 产出指标：就诊人次、平均质量分、质量优良率、平均年龄
    """
    import pyspark.sql.functions as F

    if not rows:
        return spark.createDataFrame(
            [], "stat_date string, index_code string, index_name string, index_value double"
        )

    df = spark.createDataFrame(rows)
    df = df.withColumn("stat_date", col("visit_time").substr(1, 10)) \
           .withColumn("quality_score", col("quality_score").cast("double"))

    # 1. 缺失值填充：该日期质量分均值
    avg_by_date = df.groupBy("stat_date").agg(mean("quality_score").alias("avg_q"))
    df_filled = df.join(avg_by_date, "stat_date", "left") \
        .withColumn(
            "quality_score",
            when(col("quality_score").isNull(), col("avg_q")).otherwise(col("quality_score")),
        )

    # 2. 异常值剔除：质量分需在 [0,100]，且不超过 3σ 区间
    mu = float(df_filled.select(mean("quality_score")).collect()[0][0] or 85.0)
    sigma = float(df_filled.select(F.stddev("quality_score")).collect()[0][0] or 8.0)
    df_clean = df_filled.filter(
        (col("quality_score") >= 0) & (col("quality_score") <= 100) &
        (col("quality_score") >= mu - 3 * sigma) &
        (col("quality_score") <= mu + 3 * sigma)
    )

    base = df_clean.groupBy("stat_date").agg(
        F.count("*").alias("visit_count"),
        F.mean("quality_score").alias("avg_quality"),
        F.mean("age").alias("avg_age"),
        F.mean(F.when(col("quality_score") >= 90, 1).otherwise(0)).alias("quality_rate"),
    )

    rows_out = []
    for r in base.collect():
        d = r["stat_date"]
        rows_out.append((d, "VISIT_COUNT", "就诊人次", float(r["visit_count"])))
        rows_out.append((d, "AVG_QUALITY", "平均质量分", float(r["avg_quality"])))
        rows_out.append((d, "QUALITY_RATE", "质量优良率(%)", float(r["quality_rate"]) * 100))
        rows_out.append((d, "AVG_AGE", "平均年龄", float(r["avg_age"])))

    return spark.createDataFrame(rows_out, "stat_date string, index_code string, index_name string, index_value double")


def write_bi_stat_medical(conn, rows):
    """幂等写入 bi_stat_medical：先删同日期旧数据，再插入。"""
    if conn is None or not rows:
        print("未连接数据库，跳过写入 bi_stat_medical（仅打印聚合结果）")
        return
    with conn.cursor() as cur:
        for r in rows:
            cur.execute("DELETE FROM %s WHERE stat_date = %%s" % BI_MEDICAL_TABLE, (r[0],))
        for r in rows:
            cur.execute(
                "INSERT INTO %s (stat_date, index_code, index_name, index_value, create_time) "
                "VALUES (%%s, %%s, %%s, %%s, NOW())" % BI_MEDICAL_TABLE,
                (r[0], r[1], r[2], r[3]),
            )
    conn.commit()
    print(f"已写入 {BI_MEDICAL_TABLE}: {len(rows)} 条指标记录")


def process_offline_data():
    """
    Spark SQL 离线数据标准化处理与指标集市写入：
    1) 晨会数据清洗（演示）：均值填充缺失、3σ 剔除异常值；
    2) 医疗数据聚合：data_clean_data -> bi_stat_medical（就诊人次/平均质量分/质量优良率/平均年龄）。
    """
    spark = SparkSession.builder \
        .appName("MorningMeetingOfflineDW") \
        .enableHiveSupport() \
        .getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")

    # ── 1. 晨会数据标准化处理（原有演示逻辑，保持设计文档要求）──
    print("加载原始业务数据表...")
    data = [
        ("DeptA", 50, 48, 10, 8),
        ("DeptB", 40, None, 15, 12),
        ("DeptC", 60, 55, 20, 20),
        ("DeptD", 30, 1000, 5, 2),  # 极端异常值 1000
    ]
    df = spark.createDataFrame(data, ["department", "expected_attendees", "actual_attendees", "total_issues", "resolved_issues"])

    mean_actual = df.select(mean(col("actual_attendees"))).collect()[0][0]
    df_filled = df.fillna({"actual_attendees": int(mean_actual) if mean_actual else 0})
    df_cleaned = df_filled.filter(
        (col("actual_attendees") <= col("expected_attendees")) & (col("actual_attendees") >= 0)
    )
    print("将数据写入 Hive 晨会专项数据仓库 (事实表与维度表)...")
    df_cleaned.show()
    print("离线批处理清洗完毕！")

    # ── 2. 医疗清洗数据聚合：data_clean_data -> bi_stat_medical ──
    print("读取共享库 data_clean_data 并聚合医疗指标...")
    conn = _mysql_conn()
    try:
        rows = load_clean_data(conn)
        metrics = aggregate_clean_data(spark, rows)
        metrics.show(truncate=False)
        write_bi_stat_medical(conn, metrics.collect())
    finally:
        if conn is not None:
            conn.close()


if __name__ == "__main__":
    process_offline_data()
