package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sm_approve_process_def")
public class ApproveProcessDef {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String processName;
    private Long deptId;
    private String nodesJson;
    private Integer status;
    private Date createTime;
}