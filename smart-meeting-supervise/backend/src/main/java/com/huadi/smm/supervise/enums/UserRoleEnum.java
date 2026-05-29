package com.huadi.smm.supervise.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRoleEnum {
    SUPERVISOR(1, "督办专员"),
    WORKER(2, "执行责任人"),
    PARTICIPANT(3, "晨会参会人"),
    ADMIN(4, "管理员");

    private final Integer code;
    private final String desc;
}