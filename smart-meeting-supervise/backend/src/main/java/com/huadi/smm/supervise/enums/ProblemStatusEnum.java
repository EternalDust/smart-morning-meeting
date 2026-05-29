package com.huadi.smm.supervise.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProblemStatusEnum {
    WAIT_ASSIGN(0, "待分派"),
    PROCESSING(1, "处理中"),
    WAIT_CHECK(2, "待复查"),
    CLOSED(3, "已闭环");

    private final Integer code;
    private final String desc;

    public static String getDesc(Integer code) {
        for (ProblemStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status.desc;
            }
        }
        return "未知";
    }
}
