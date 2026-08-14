package com.huadi.smm.collection.flink;

import cn.hutool.json.JSONUtil;
import org.apache.flink.api.common.functions.RichMapFunction;

import java.util.Map;
import java.util.Set;

/**
 * 数据格式校验——过滤掉关键字段为空或格式明显错误的记录
 * 与上报入口 DataContentValidator 的校验口径保持一致（此处不依赖 common，内联实现）
 */
public class DataValidationMapper extends RichMapFunction<String, String> {

    private static final Set<String> ALLOWED_GENDERS = Set.of("男", "女", "M", "F", "0", "1");
    private static final Set<String> ALLOWED_DOMAINS =
            Set.of("HIS", "LIS", "EMR", "PACS", "DRUG", "MEETING", "GENERAL");

    @Override
    public String map(String value) throws Exception {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            Map<String, Object> data = JSONUtil.parseObj(value);
            // 关键字段非空校验
            if (isEmpty(data.get("patientId")) || isEmpty(data.get("visitTime"))) {
                return null;
            }
            // 数据源与数据域必须存在且在允许范围内
            if (isEmpty(data.get("sourceCode")) || isEmpty(data.get("dataDomain"))) {
                return null;
            }
            if (!ALLOWED_DOMAINS.contains(data.get("dataDomain").toString().toUpperCase())) {
                return null;
            }
            // 就诊时间格式校验
            if (!isValidVisitTime(data.get("visitTime"))) {
                return null;
            }
            // 性别/年龄取值白名单
            if (data.get("gender") != null && !isEmpty(data.get("gender"))
                    && !ALLOWED_GENDERS.contains(data.get("gender").toString().trim())) {
                return null;
            }
            if (data.get("age") != null && !isEmpty(data.get("age"))
                    && !isValidAge(data.get("age"))) {
                return null;
            }
            return value;
        } catch (Exception e) {
            return null; // 非法的JSON格式，丢弃
        }
    }

    private boolean isEmpty(Object v) {
        return v == null || "".equals(v.toString().trim());
    }

    private boolean isValidVisitTime(Object v) {
        String s = v.toString().trim();
        // 支持 yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd
        return s.matches("\\d{4}-\\d{2}-\\d{2}( \\d{2}:\\d{2}:\\d{2})?");
    }

    private boolean isValidAge(Object v) {
        try {
            int age = Integer.parseInt(v.toString().trim());
            return age >= 0 && age <= 130;
        } catch (Exception e) {
            return false;
        }
    }
}
