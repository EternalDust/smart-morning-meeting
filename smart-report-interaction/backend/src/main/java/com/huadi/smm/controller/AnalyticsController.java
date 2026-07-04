package com.huadi.smm.controller;

import com.huadi.smm.common.Result;
import com.huadi.smm.service.AnalyticsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Resource
    private AnalyticsService analyticsService;

    @GetMapping("/meeting/{meetingId}")
    public Result<?> meetingDetail(@PathVariable Long meetingId) {
        return Result.ok(analyticsService.getByMeetingId(meetingId));
    }

    @GetMapping("/meetings/trend")
    public Result<?> meetingTrend() {
        return Result.ok(analyticsService.getMeetingTrend());
    }

    @GetMapping("/departments")
    public Result<?> departments() {
        return Result.ok(analyticsService.getDepartmentRanking());
    }

    @GetMapping("/member/{userId}")
    public Result<?> memberProfile(@PathVariable String userId) {
        return Result.ok(analyticsService.getByUserId(userId));
    }
}
