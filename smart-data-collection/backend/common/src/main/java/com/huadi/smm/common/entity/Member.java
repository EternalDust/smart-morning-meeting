package com.huadi.smm.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 共享人员表 sm_gm_members
 * 与团队统一账号体系对齐：role 1=管理层 2=科室人员；status 1=启用 0=禁用
 */
@Data
@TableName("sm_gm_members")
public class Member {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userId;
    private String name;
    private Integer role;
    private String dept;
    private String password;
    private Integer status;
}
