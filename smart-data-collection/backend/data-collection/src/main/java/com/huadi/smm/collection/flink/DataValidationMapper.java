package com.huadi.smm.collection.flink;

import cn.hutool.json.JSONUtil;
import org.apache.flink.api.common.functions.RichMapFunction;

import java.util.Map;

/**
 * 数据格式校验——过滤掉关键字段为空或格式明显错误的记录
 */
public class DataValidationMapper extends RichMapFunction<String, String> {

    @Override
    public String map(String value) throws Exception {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            Map<String, Object> data = JSONUtil.parseObj(value);
            // 关键字段为空则丢弃
            if (data.containsKey("patientId") && (data.get("patientId") == null
                    || "".equals(data.get("patientId").toString()))) {
                return null;
            }
            return value;
        } catch (Exception e) {
            return null; // 非法的JSON格式，丢弃
        }
    }
}
