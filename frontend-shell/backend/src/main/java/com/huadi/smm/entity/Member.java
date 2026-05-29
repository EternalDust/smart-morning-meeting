package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sm_gm_members")
public class Member {
    private Long id;
    private String userId;
    private String name;
    private String password;
    private Integer role;
    private String dept;
}
