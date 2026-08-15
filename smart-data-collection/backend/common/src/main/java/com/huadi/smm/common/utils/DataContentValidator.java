package com.huadi.smm.common.utils;

import com.huadi.smm.common.enums.DataSourceType;
import com.huadi.smm.common.enums.MedicalDataDomain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;

/**
 * 采集上报数据内容校验器
 * 解决"数据只管往数据管道里扔、内容不做任何限制"的问题：
 * 对数据源类型、数据域、必填字段、字段格式与取值做统一校验。
 * 返回 null 表示校验通过，否则返回具体错误文案。
 */
public final class DataContentValidator {

    public static final String[] REQUIRED_FIELDS = {"patientId", "visitTime"};

    private static final Set<String> ALLOWED_GENDERS = Set.of("男", "女", "M", "F", "0", "1");
    private static final DateTimeFormatter DATETIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DataContentValidator() {
    }

    /**
     * 逐条校验，命中即返回对应错误文案；全部通过返回 null
     */
    public static String validateContent(String sourceType, String dataDomain,
                                         Map<String, Object> data) {
        if (DataSourceType.fromCode(sourceType) == null) {
            return "数据源类型不允许：" + sourceType;
        }
        if (MedicalDataDomain.fromCode(dataDomain) == null) {
            return "数据域不在允许范围内：" + dataDomain;
        }
        if (isBlank(data.get("patientId"))) {
            return "缺少必填字段 patientId";
        }
        if (isBlank(data.get("visitTime"))) {
            return "缺少必填字段 visitTime";
        }
        if (data.get("patientId").toString().length() > 50) {
            return "patientId 长度超过50";
        }
        if (!isValidVisitTime(data.get("visitTime"))) {
            return "visitTime 格式错误，应为 yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd";
        }
        if (data.get("age") != null && !isBlank(data.get("age")) && !isValidAge(data.get("age"))) {
            return "age 非法，应为 0~130 的整数";
        }
        if (data.get("gender") != null && !isBlank(data.get("gender"))
                && !isValidGender(data.get("gender"))) {
            return "gender 非法，应为 男/女/M/F/0/1";
        }
        return null;
    }

    public static boolean isBlank(Object value) {
        return value == null || "".equals(value.toString().trim());
    }

    public static boolean isValidVisitTime(Object v) {
        String s = v.toString().trim();
        try {
            LocalDateTime.parse(s, DATETIME_FMT);
            return true;
        } catch (Exception ignored) {
            // fall through
        }
        try {
            LocalDate.parse(s, DATE_FMT);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidAge(Object v) {
        try {
            int age = Integer.parseInt(v.toString().trim());
            return age >= 0 && age <= 130;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidGender(Object v) {
        return ALLOWED_GENDERS.contains(v.toString().trim());
    }
}
