package com.huadi.smm.supervise.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sm_gm_members")  // 使用共享表
public class User {
    private Long id;
    @TableField("user_id")
    private String userId;
    private String password;
    private String name;
    private Integer role;
    private String dept;
    private Integer status;
}
