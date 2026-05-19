package com.huadi.smm.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.huadi.smm.common.ApiResponse;
import com.huadi.smm.entity.BiStatMeeting;
import com.huadi.smm.entity.BiStatSupervise;
import com.huadi.smm.service.BiStatMeetingService;
import com.huadi.smm.service.BiStatSuperviseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final BiStatMeetingService biStatMeetingService;
    private final BiStatSuperviseService biStatSuperviseService;

    @GetMapping("/trend")
    public ApiResponse<Map<String, Object>> getTrendData(jakarta.servlet.http.HttpServletRequest request) {
        Long loginDeptId = null;
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            try {
                io.jsonwebtoken.Claims claims = com.huadi.smm.utils.JwtUtils.parseToken(token.substring(7));
                Integer roleId = claims.get("roleId", Integer.class);
                if (roleId != null && (roleId == 2 || roleId == 3)) { // 2 为中层 3为基层
                    loginDeptId = claims.get("deptId", Long.class);
                }
            } catch (Exception e) {}
        }

        // 按日期查询近7日的参会率
        QueryWrapper<BiStatMeeting> query = new QueryWrapper<>();
        if (loginDeptId != null) {
            query.eq("dept_id", loginDeptId);
        }
        query.orderByDesc("stat_date").last("LIMIT 7");
        List<BiStatMeeting> list = biStatMeetingService.list(query);

        List<String> dates = new ArrayList<>();
        List<BigDecimal> rates = new ArrayList<>();

        // 如果查询出来是降序(最新日期在前)，则反转使其变为从旧到新展示
        java.util.Collections.reverse(list);

        if (list.isEmpty()) {
            dates.addAll(List.of("周一", "周二", "周三", "周四", "周五", "周六", "周日"));
            rates.addAll(List.of(new BigDecimal("92"), new BigDecimal("95"), new BigDecimal("88"), new BigDecimal("98"), new BigDecimal("96"), new BigDecimal("99"), new BigDecimal("93")));
        } else {
            for (BiStatMeeting meeting : list) {
                dates.add(meeting.getStatDate());
                rates.add(meeting.getAttendRate() != null ? meeting.getAttendRate() : BigDecimal.ZERO);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("rates", rates);
        return ApiResponse.success(result);
    }

    @GetMapping("/issues-distribution")
    public ApiResponse<List<Map<String, Object>>> getIssuesDistribution(jakarta.servlet.http.HttpServletRequest request) {
        Long loginDeptId = null;
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            try {
                io.jsonwebtoken.Claims claims = com.huadi.smm.utils.JwtUtils.parseToken(token.substring(7));
                Integer roleId = claims.get("roleId", Integer.class);
                if (roleId != null && (roleId == 2 || roleId == 3)) { // 2 为中层, 3为基层
                    loginDeptId = claims.get("deptId", Long.class);
                }
            } catch (Exception e) {}
        }

        // 从督办表查询各个科室的问题总数
        QueryWrapper<BiStatSupervise> query = new QueryWrapper<>();
        if (loginDeptId != null) {
            query.eq("dept_id", loginDeptId);
        }
        List<BiStatSupervise> list = biStatSuperviseService.list(query);
        List<Map<String, Object>> result = new ArrayList<>();

        if (list.isEmpty()) {
            // 没有数据时返回 mock 数据
            result.add(Map.of("name", "心内科", "value", 1048));
            result.add(Map.of("name", "神经内科", "value", 735));
            result.add(Map.of("name", "急诊科", "value", 580));
            result.add(Map.of("name", "外科", "value", 484));
        } else {
            for (BiStatSupervise sup : list) {
                Map<String, Object> map = new HashMap<>();
                // 此处将 deptId 作为名字展示，实际应用应当关联字典表或部门表查出 dept_name
                map.put("name", "科室 " + sup.getDeptId()); 
                map.put("value", sup.getTotalCount() != null ? sup.getTotalCount() : 0);
                result.add(map);
            }
        }
        return ApiResponse.success(result);
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
        mock.setAttendRate(new BigDecimal("96.00"));
        mock.setCreateTime("2026-05-19 08:30:00");
        
        boolean saved = biStatMeetingService.save(mock);
        return saved ? ApiResponse.success("测试数据插入成功！") : ApiResponse.error(500, "插入失败");
    }
}
