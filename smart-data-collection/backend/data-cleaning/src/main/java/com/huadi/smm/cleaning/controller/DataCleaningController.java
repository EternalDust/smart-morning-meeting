package com.huadi.smm.cleaning.controller;

import com.huadi.smm.cleaning.service.DataCleaningService;
import com.huadi.smm.common.entity.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cleaning")
public class DataCleaningController {

    @Autowired
    private DataCleaningService dataCleaningService;

    @PostMapping("/trigger")
    public R triggerCleaning(@RequestParam(defaultValue = "batch") String type) {
        dataCleaningService.triggerCleaning(type);
        return R.ok().message("清洗任务已触发，类型：" + type);
    }

    @GetMapping("/stats")
    public R getCleaningStats() {
        return R.ok(dataCleaningService.getCleaningStats());
    }

    @GetMapping("/quality-trend")
    public R getQualityTrend(@RequestParam(defaultValue = "7") int days) {
        return R.ok(dataCleaningService.getQualityTrend(days));
    }

    /**
     * 标准数据表明细（供可视化/大数据下游读取；GET 类接口对任意已认证用户开放）
     */
    @GetMapping("/data/list")
    public R getCleanDataList(@RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "20") int size,
                              @RequestParam(required = false) String department) {
        return R.ok(dataCleaningService.getCleanDataList(page, size, department));
    }

    /**
     * 按科室聚合统计（科室维度即与晨会数据的关联键：department → sm_gm_members.dept）
     */
    @GetMapping("/data/by-department")
    public R getCleanDataByDepartment() {
        return R.ok(dataCleaningService.getCleanDataByDepartment());
    }
}
