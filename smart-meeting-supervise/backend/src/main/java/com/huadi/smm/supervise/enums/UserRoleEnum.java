package com.huadi.smm.supervise.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 平台统一账号角色模型：
 * 管理员（工号 2 开头）/ 参会人（工号 1 开头）。
 * “执行责任人 / 督办专员”是业务流程角色，由任务分派决定，不作为账号属性。
 */
@Getter
@AllArgsConstructor
public enum UserRoleEnum {
    ADMIN("管理员"),
    MEMBER("参会人");

    private final String desc;

    public static boolean isAdmin(String userId) {
        return userId != null && userId.startsWith("2");
    }
}
