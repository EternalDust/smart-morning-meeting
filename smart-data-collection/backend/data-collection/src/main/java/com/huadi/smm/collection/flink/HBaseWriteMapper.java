package com.huadi.smm.collection.flink;

import org.apache.flink.api.common.functions.RichMapFunction;

/**
 * 将校验通过的原始数据写入HBase存储
 * 实际部署时需要替换为HBase connector实现
 */
public class HBaseWriteMapper extends RichMapFunction<String, String> {

    @Override
    public String map(String value) throws Exception {
        if (value == null) {
            return null;
        }
        // HBase写入逻辑：根据数据内容构建Put操作
        // 生产环境中使用 HBaseSinkFunction 或 Phoenix connector
        return value;
    }
}
