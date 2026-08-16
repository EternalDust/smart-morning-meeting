package com.huadi.smm.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.huadi.smm.common.ApiResponse;
import com.huadi.smm.common.UserContext;
import com.huadi.smm.entity.BiStatMeeting;
import com.huadi.smm.service.BiStatMeetingService;
import com.huadi.smm.service.DashboardAggregateService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 大屏数据接口。
 *
 * 数据范围统一走 UserContext：
 * 管理层（role=1，或无 token 演示模式）→ 全院数据；
 * 科室人员（role=2）→ 仅本科室成员的数据。
 * 聚合逻辑见 {@link DashboardAggregateService}，直接读共享库真实业务表。
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardAggregateService dashboardAggregateService;
    private final BiStatMeetingService biStatMeetingService;
    private final UserContext userContext;

    /** 近七日参会率趋势（真实聚合，科室人员按本科室过滤） */
    @GetMapping("/trend")
    public ApiResponse<Map<String, Object>> getTrendData(HttpServletRequest request) {
        return ApiResponse.success(dashboardAggregateService.getTrend(userContext.currentDept(request)));
    }

    /** 会议数据概览：应到/实到/参会率/发言/互动/质量分，按日期 */
    @GetMapping("/meeting-overview")
    public ApiResponse<List<Map<String, Object>>> getMeetingOverview(HttpServletRequest request) {
        return ApiResponse.success(dashboardAggregateService.getMeetingOverview(userContext.currentDept(request)));
    }

    /** 问题部门分布（sm_problem 按归属科室聚合） */
    @GetMapping("/issues-distribution")
    public ApiResponse<List<Map<String, Object>>> getIssuesDistribution(HttpServletRequest request) {
        return ApiResponse.success(dashboardAggregateService.getIssuesDistribution(userContext.currentDept(request)));
    }

    @GetMapping("/base-level/data")
    public ApiResponse<List<BiStatMeeting>> getBaseLevelData() {
        QueryWrapper<BiStatMeeting> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("stat_date", "create_time").last("LIMIT 10");
        List<BiStatMeeting> list = biStatMeetingService.list(queryWrapper);
        return ApiResponse.success(list);
    }

    @PostMapping("/test-insert")
    public ApiResponse<String> insertMockData() {
        BiStatMeeting mock = new BiStatMeeting();
        mock.setStatDate("2026-05-19");
        mock.setDeptName("心内科");
        mock.setMeetingCount(1);
        mock.setShouldNum(50);
        mock.setRealNum(48);
        mock.setAttendRate(new java.math.BigDecimal("96.00"));
        mock.setCreateTime("2026-05-19 08:30:00");

        boolean saved = biStatMeetingService.save(mock);
        return saved ? ApiResponse.success("测试数据插入成功！") : ApiResponse.error(500, "插入失败");
    }
}
