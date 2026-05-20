package com.huadi.smm.workbench.controller;

import com.huadi.smm.common.entity.R;
import com.huadi.smm.workbench.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/overview")
    public R getOverview() {
        return R.ok(dashboardService.getOverview());
    }

    @GetMapping("/source-status")
    public R getSourceStatus() {
        return R.ok(dashboardService.getSourceStatus());
    }

    @GetMapping("/quality-trend")
    public R getQualityTrend(@RequestParam(defaultValue = "7") int days) {
        return R.ok(dashboardService.getQualityTrend(days));
    }

    @GetMapping("/processing-delay")
    public R getProcessingDelay() {
        return R.ok(dashboardService.getProcessingDelay());
    }
}
