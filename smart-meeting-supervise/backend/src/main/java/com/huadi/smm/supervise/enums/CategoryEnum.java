package com.huadi.smm.supervise.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CategoryEnum {
    MEDICAL(1, "医疗类"),
    OPERATION(2, "运维类"),
    MANAGEMENT(3, "管理类");

    private final Integer code;
    private final String desc;
}