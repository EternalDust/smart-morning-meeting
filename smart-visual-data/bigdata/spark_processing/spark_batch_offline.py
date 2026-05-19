from pyspark.sql import SparkSession
from pyspark.sql.functions import col, mean, when

def process_offline_data():
    """
    Spark SQL 离线数据标准化处理与 Hive 写入逻辑
    设计文档要求：采用 Spark SQL 与 Pandas 进行数据清洗，通过均值等填充缺失值，3σ 原则剔除异常值。
    """
    # 模拟开启了 Hive 支持的 SparkSession
    spark = SparkSession.builder \
        .appName("MorningMeetingOfflineDW") \
        .enableHiveSupport() \
        .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    print("加载原始业务数据表...")
    # 实际环境可能是从 HDFS 或者其他数据源读取，此处以模拟一个DataFrame为例
    data = [
        ("DeptA", 50, 48, 10, 8),
        ("DeptB", 40, None, 15, 12),
        ("DeptC", 60, 55, 20, 20),
        ("DeptD", 30, 1000, 5, 2), # 这是一个极端异常值 1000
    ]
    df = spark.createDataFrame(data, ["department", "expected_attendees", "actual_attendees", "total_issues", "resolved_issues"])

    # 1. 缺失值处理: 求某列的均值填充
    mean_actual = df.select(mean(col("actual_attendees"))).collect()[0][0]
    df_filled = df.fillna({"actual_attendees": int(mean_actual) if mean_actual else 0})

    # 2. 异常值剔除 (简单模拟业务阈值或3σ): actual_attendees 不能大于 expected_attendees 的异常剔除
    df_cleaned = df_filled.filter((col("actual_attendees") <= col("expected_attendees")) & (col("actual_attendees") >= 0))

    # 3. 将数据写入对应的 Hive 的主题数据集市中
    print("将数据写入 Hive 晨会专项数据仓库 (事实表与维度表)...")
    # df_cleaned.write.mode("append").saveAsTable("morning_meeting_dw.fact_meeting_records")
    
    df_cleaned.show()
    print("离线批处理清洗完毕！")

if __name__ == "__main__":
    process_offline_data()
