package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sm_approve_record")
public class ApproveRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long meetingId;
    private Long processId;
    private String nodeName;
    private Long approverId;
    private Integer action;
    private String opinion;
    private Date approveTime;
}