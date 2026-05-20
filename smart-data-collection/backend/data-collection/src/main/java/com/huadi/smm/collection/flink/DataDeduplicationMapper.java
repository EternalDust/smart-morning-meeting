package com.huadi.smm.collection.flink;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.Configuration;

/**
 * 滑动窗口去重——用带TTL的MapState替代全局状态，避免RocksDB膨胀
 * 同一业务主键在30分钟内只保留第一条
 */
public class DataDeduplicationMapper extends RichMapFunction<String, String> {

    private transient MapState<String, Boolean> seenState;

    @Override
    public void open(Configuration parameters) {
        MapStateDescriptor<String, Boolean> descriptor =
                new MapStateDescriptor<>("seen-records", String.class, Boolean.class);

        StateTtlConfig ttlConfig = StateTtlConfig
                .newBuilder(Time.minutes(30))
                .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
                .build();
        descriptor.enableTimeToLive(ttlConfig);

        seenState = getRuntimeContext().getMapState(descriptor);
    }

    @Override
    public String map(String value) throws Exception {
        String key = extractKey(value);
        if (seenState.contains(key)) {
            return null; // 重复数据，过滤掉
        }
        seenState.put(key, true);
        return value;
    }

    private String extractKey(String json) {
        // 基于"患者ID+就诊时间"的联合键去重
        try {
            int pidStart = json.indexOf("\"patientId\"");
            int pidEnd = json.indexOf(",", pidStart > 0 ? pidStart : 0);
            int vtStart = json.indexOf("\"visitTime\"");
            int vtEnd = json.indexOf(",", vtStart > 0 ? vtStart : 0);
            if (pidStart > 0 && vtStart > 0) {
                return json.substring(Math.max(0, pidStart), Math.min(json.length(), Math.max(pidEnd, vtEnd)));
            }
        } catch (Exception ignored) {
        }
        return String.valueOf(json.hashCode());
    }
}
