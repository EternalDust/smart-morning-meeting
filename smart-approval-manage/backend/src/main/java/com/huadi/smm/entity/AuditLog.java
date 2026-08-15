package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("sm_audit_log")
public class AuditLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String operationType;
    private Long targetId;
    private String targetType;
    private Long operatorId;
    private String operatorName;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private Date createTime;
}