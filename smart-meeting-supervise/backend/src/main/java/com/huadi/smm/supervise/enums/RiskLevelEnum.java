package com.huadi.smm.supervise.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RiskLevelEnum {
    GENERAL(1, "一般"),
    IMPORTANT(2, "重要"),
    EMERGENCY(3, "紧急");

    private final Integer code;
    private final String desc;
}
