package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sm_gm_members")
public class GmMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String workNo;
    private String realName;
    private Long deptId;
    private Integer role;
}