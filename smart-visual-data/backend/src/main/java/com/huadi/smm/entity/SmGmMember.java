package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 平台人员表（共享表 sm_gm_members）
 * 账号体系统一后，登录与数据范围均以该表为准：
 * role = 1 管理层（工号 2 开头），可看全院数据；
 * role = 2 科室人员（工号 1 开头），仅看本科室数据。
 */
@Data
@TableName("sm_gm_members")
public class SmGmMember {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户账号（工号），统一 JWT 的 subject */
    private String userId;

    private String name;

    /** 角色：1 管理层 2 科室人员 */
    private Integer role;

    /** 科室（如：外科 / 内科 / 管理层） */
    private String dept;

    private String password;

    /** 状态：1 启用 0 禁用 */
    private Integer status;
}
