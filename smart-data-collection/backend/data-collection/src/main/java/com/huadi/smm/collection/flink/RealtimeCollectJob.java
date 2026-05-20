package com.huadi.smm.collection.flink;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * Flink 实时采集作业——消费 Kafka 原始数据并写入 HBase
 * 使用滑动窗口去重策略，避免全局状态膨胀
 */
public class RealtimeCollectJob {

    private static final String KAFKA_BROKERS = "localhost:9092";
    private static final String RAW_TOPIC = "raw-data-topic";
    private static final String CONSUMER_GROUP = "flink-collect-group";

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(60000); // 每分钟做一次 Checkpoint，保障断点续传

        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers(KAFKA_BROKERS)
                .setTopics(RAW_TOPIC)
                .setGroupId(CONSUMER_GROUP)
                .setStartingOffsets(OffsetsInitializer.committedOffsets())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        DataStream<String> rawStream = env
                .fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka-Raw-Data-Source");

        rawStream
                .map(new DataDeduplicationMapper())  // 滑动窗口去重
                .map(new DataValidationMapper())     // 格式校验
                .map(new HBaseWriteMapper());        // 写入HBase

        env.execute("RealtimeCollectJob - 医疗多源数据实时采集");
    }
}
