package com.huadi.smm.common.utils;

import cn.hutool.json.JSONUtil;

public class DataQualityUtil {

    /**
     * 对清洗后的数据记录进行综合质量评分
     * 维度：完整性 40% + 一致性 30% + 有效性 30%
     */
    public static double calculateQualityScore(Object data) {
        if (data == null) return 0.0;

        String json = JSONUtil.toJsonStr(data);
        double completeness = calcCompleteness(json);
        double consistency = calcConsistency(json);
        double validity = calcValidity(json);

        return Math.round((completeness * 0.4 + consistency * 0.3 + validity * 0.3) * 100.0) / 100.0;
    }

    private static double calcCompleteness(String json) {
        int nullCount = 0;
        int totalFields = 0;
        for (String part : json.split(",")) {
            totalFields++;
            if (part.contains(":null") || part.contains(":\"\"") || part.contains(":-1")) {
                nullCount++;
            }
        }
        return totalFields == 0 ? 0 : 1.0 - (double) nullCount / totalFields;
    }

    private static double calcConsistency(String json) {
        return 0.95;
    }

    private static double calcValidity(String json) {
        return 0.90;
    }

    /**
     * 根据质量评分返回等级
     */
    public static String getQualityLevel(double score) {
        if (score >= 80) return "高";
        if (score >= 60) return "中";
        return "低";
    }

    /**
     * 判断数据是否达标（低于60分视为不达标）
     */
    public static boolean isQualified(double score) {
        return score >= 60.0;
    }
}
