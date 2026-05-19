package com.smartmeeting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sm_gm_members")
public class Member {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String userId;
    private String name;
    private Integer role;
    private String dept;
    private Integer status;
}
