package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("sm_approve_task")
public class ApproveTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long meetingId;
    private String nodeId;
    private String nodeType;
    private Long approverId;
    private Integer status;
    private Integer action;
    private String opinion;
    private Date approveTime;
}