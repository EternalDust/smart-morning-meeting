package com.huadi.smm.cleaning.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;

import static org.apache.spark.sql.functions.*;

/**
 * Spark离线数据清洗作业
 * 功能：去重、格式转换、空值填充、质量评分、异常数据分离
 */
public class DataCleaningJob {

    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder()
                .appName("DataCleaningJob")
                .enableHiveSupport()
                .config("spark.sql.adaptive.enabled", "true")
                .config("spark.sql.adaptive.coalescePartitions.enabled", "true")
                .getOrCreate();

        String partition = args.length > 0 ? args[0] : "20240508";

        // ---- 第1步：读取原始数据 ----
        Dataset<Row> rawDf = spark.sql(
                "SELECT * FROM ods_raw_data WHERE dt='" + partition + "'");

        // ---- 第2步：去重（基于 patient_id + visit_time 联合键，保留最新一条） ----
        WindowSpec windowSpec = Window.partitionBy("patient_id", "visit_time")
                .orderBy(col("collect_time").desc());

        Dataset<Row> dedupedDf = rawDf
                .withColumn("rn", row_number().over(windowSpec))
                .where(col("rn").equalTo(1))
                .drop("rn");

        // ---- 第3步：格式标准化 ----
        Dataset<Row> formattedDf = dedupedDf
                .withColumn("gender",
                        when(col("gender").equalTo("男"), "0")
                                .when(col("gender").equalTo("女"), "1")
                                .otherwise(col("gender")))
                .withColumn("visit_time",
                        to_timestamp(col("visit_time"), "yyyy-MM-dd HH:mm:ss"));

        // ---- 第4步：空值填充 ----
        Dataset<Row> filledDf = formattedDf
                .withColumn("age",
                        when(col("age").isNull().or(col("age").equalTo("")), -1)
                                .otherwise(col("age").cast("int")))
                .withColumn("diagnosis",
                        when(col("diagnosis").isNull().or(col("diagnosis").equalTo("")),
                                "未知")
                                .otherwise(col("diagnosis")));

        // ---- 第5步：质量评分（基于完整性、一致性、有效性） ----
        Dataset<Row> scoredDf = filledDf
                .withColumn("completeness", expr("CASE WHEN age = -1 THEN 0.6 ELSE 1.0 END"))
                .withColumn("consistency", lit(0.95))
                .withColumn("validity", expr("CASE WHEN gender IN ('0','1') THEN 1.0 ELSE 0.5 END"))
                .withColumn("quality_score",
                        round(col("completeness").multiply(0.4)
                                .plus(col("consistency").multiply(0.3))
                                .plus(col("validity").multiply(0.3))
                                .multiply(100), 2))
                .drop("completeness", "consistency", "validity");

        // ---- 第6步：分离正常数据与异常数据 ----
        // 质量评分 >= 60 的写入清洗后数据表
        scoredDf.where(col("quality_score").geq(60))
                .write()
                .mode("overwrite")
                .insertInto("dwd_clean_data");

        // 质量评分 < 60 的写入异常数据表
        scoredDf.where(col("quality_score").lt(60))
                .withColumn("anomaly_reason",
                        expr("CONCAT('质量评分过低: ', CAST(quality_score AS STRING))"))
                .write()
                .mode("overwrite")
                .insertInto("dwd_anomaly_data");

        spark.close();
    }
}
