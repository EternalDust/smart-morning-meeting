package com.huadi.smm.common.enums;

/**
 * 医疗数据域界定
 * 用于限定采集数据所属的医疗业务范围，避免"只管往数据管道里扔、内容不做限制"
 */
public enum MedicalDataDomain {

    HIS("HIS", "HIS门诊"),
    LIS("LIS", "LIS检验"),
    EMR("EMR", "电子病历"),
    PACS("PACS", "PACS影像"),
    DRUG("DRUG", "药品管理"),
    MEETING("MEETING", "晨会数据"),
    GENERAL("GENERAL", "通用/未分类");

    private final String code;
    private final String desc;

    MedicalDataDomain(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static MedicalDataDomain fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (MedicalDataDomain d : values()) {
            if (d.code.equalsIgnoreCase(code)) {
                return d;
            }
        }
        return null;
    }

    /**
     * 根据数据源编码（如 MYSQL_HIS_001）与名称（如 HIS门诊业务库）自动推断数据域。
     * 同时兼容英文关键词与中文关键词，均未命中返回 GENERAL。
     */
    public static MedicalDataDomain derive(String sourceCode, String sourceName) {
        String c = (sourceCode == null ? "" : sourceCode).toUpperCase();
        String n = (sourceName == null ? "" : sourceName).toUpperCase();

        if (c.contains("HIS") || n.contains("HIS") || n.contains("门诊")) {
            return HIS;
        }
        if (c.contains("LIS") || n.contains("LIS") || n.contains("检验")) {
            return LIS;
        }
        if (c.contains("EMR") || n.contains("EMR") || n.contains("病历")) {
            return EMR;
        }
        if (c.contains("PACS") || n.contains("PACS") || n.contains("影像")) {
            return PACS;
        }
        if (c.contains("DRUG") || n.contains("DRUG") || n.contains("药品")) {
            return DRUG;
        }
        if (c.contains("MEETING") || n.contains("MEETING") || n.contains("晨会")) {
            return MEETING;
        }
        return GENERAL;
    }
}
