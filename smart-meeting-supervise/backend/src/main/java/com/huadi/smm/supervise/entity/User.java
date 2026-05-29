package com.huadi.smm.supervise.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sm_gm_members")  // 使用共享表
public class User {
    private Long id;
    private String account;
    private String password;
    private String name;
    private Integer role;
    private String dept;
    private String phone;
    private Integer status;
}