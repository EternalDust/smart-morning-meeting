package com.huadi.smm.controller;

import com.huadi.smm.config.Result;
import com.huadi.smm.entity.AuditLog;
import com.huadi.smm.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public Result<List<AuditLog>> list() {
        return Result.ok(auditLogService.lambdaQuery().orderByDesc(AuditLog::getCreateTime).list());
    }
}