package com.huadi.smm.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huadi.smm.dao.AuditLogMapper;
import com.huadi.smm.entity.AuditLog;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class AuditLogService extends ServiceImpl<AuditLogMapper, AuditLog> {
    public void log(String operationType, Long targetId, String targetType, Long operatorId, String detail) {
        AuditLog log = new AuditLog();
        log.setOperationType(operationType);
        log.setTargetId(targetId);
        log.setTargetType(targetType);
        log.setOperatorId(operatorId);
        log.setNewValue(detail);
        log.setCreateTime(new Date());
        save(log);
    }
}