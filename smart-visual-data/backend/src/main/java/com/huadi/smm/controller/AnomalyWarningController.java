package com.huadi.smm.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huadi.smm.common.ApiResponse;
import com.huadi.smm.entity.BiWarnRecord;
import com.huadi.smm.service.BiWarnRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warn")
@RequiredArgsConstructor
public class AnomalyWarningController {

    private final BiWarnRecordService warnRecordService;

    @GetMapping("/list")
    public ApiResponse<Page<BiWarnRecord>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer warnLevel,
            @RequestParam(required = false) String warnType) {

        QueryWrapper<BiWarnRecord> query = new QueryWrapper<>();
        query.orderByDesc("create_time");

        if (warnLevel != null) {
            query.eq("warn_level", warnLevel);
        }
        if (warnType != null && !warnType.isEmpty()) {
            query.eq("warn_type", warnType);
        }

        Page<BiWarnRecord> result = warnRecordService.page(new Page<>(page, size), query);
        return ApiResponse.success(result);
    }

    @GetMapping("/latest")
    public ApiResponse<List<BiWarnRecord>> getLatest(@RequestParam(defaultValue = "10") int limit) {
        QueryWrapper<BiWarnRecord> query = new QueryWrapper<>();
        query.orderByDesc("create_time").last("LIMIT " + limit);
        List<BiWarnRecord> list = warnRecordService.list(query);
        return ApiResponse.success(list);
    }

    @PutMapping("/handle/{id}")
    public ApiResponse<String> handleWarn(@PathVariable Long id, @RequestBody BiWarnRecord record) {
        BiWarnRecord existing = warnRecordService.getById(id);
        if (existing == null) {
            return ApiResponse.error(404, "预警记录不存在");
        }
        existing.setStatus(1);
        existing.setHandlerId(record.getHandlerId());
        existing.setHandleTime(record.getHandleTime());
        warnRecordService.updateById(existing);
        return ApiResponse.success("预警已处理");
    }
}
