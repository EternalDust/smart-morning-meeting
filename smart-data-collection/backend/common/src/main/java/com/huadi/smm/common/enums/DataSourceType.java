package com.huadi.smm.common.enums;

import java.util.Set;

/**
 * 数据源类型白名单
 * 对应 data_source_config.source_type，用于采集入口的数据源范围校验
 */
public enum DataSourceType {

    MYSQL("mysql", "关系型数据库"),
    KAFKA("kafka", "消息队列"),
    HTTP("http", "HTTP接口"),
    MONGODB("mongodb", "文档数据库");

    private final String code;
    private final String desc;

    DataSourceType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 忽略大小写匹配，null/未匹配返回 null
     */
    public static DataSourceType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (DataSourceType t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        return null;
    }

    public static Set<String> allowedCodes() {
        return Set.of("mysql", "kafka", "http", "mongodb");
    }
}
