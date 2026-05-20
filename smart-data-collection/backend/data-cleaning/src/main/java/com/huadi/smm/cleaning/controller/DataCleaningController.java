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
}
