package com.huadi.smm.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.huadi.smm.common.ApiResponse;
import com.huadi.smm.entity.BiReviewReport;
import com.huadi.smm.service.BiReviewReportService;
import com.huadi.smm.service.ReviewReportService;
import com.huadi.smm.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 大模型复盘报告接口
 * 文档要求：报告与决策建议仅中层及以上角色（role_id 1/2）可查看与生成。
 * 演示模式（未携带 Token）时放通，便于联调；带 Token 时做角色校验。
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class ReviewReportController {

    private final ReviewReportService reviewReportService;
    private final BiReviewReportService biReviewReportService;

    /** 生成复盘报告 / 管理决策建议 */
    @PostMapping("/review-report")
    public ApiResponse<BiReviewReport> generate(@RequestBody GenerateReportRequest req, HttpServletRequest request) {
        if (!canAccess(request)) {
            return ApiResponse.error(403, "无权限：复盘报告仅中层及以上角色可生成");
        }
        BiReviewReport report = reviewReportService.generateReport(
                req.getReportType(), req.getStartDate(), req.getEndDate(), operatorName(request));
        return ApiResponse.success(report);
    }

    /** 获取最新报告 */
    @GetMapping("/review-report/latest")
    public ApiResponse<BiReviewReport> latest(
            @RequestParam(defaultValue = "REVIEW") String reportType, HttpServletRequest request) {
        if (!canAccess(request)) {
            return ApiResponse.error(403, "无权限");
        }
        QueryWrapper<BiReviewReport> query = new QueryWrapper<>();
        query.eq("report_type", reportType).orderByDesc("create_time").last("LIMIT 1");
        return ApiResponse.success(biReviewReportService.getOne(query));
    }

    /** 按时间范围查看历史报告 */
    @GetMapping("/review-report/list")
    public ApiResponse<List<BiReviewReport>> list(
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletRequest request) {
        if (!canAccess(request)) {
            return ApiResponse.error(403, "无权限");
        }
        QueryWrapper<BiReviewReport> query = new QueryWrapper<>();
        if (StringUtils.hasText(reportType)) {
            query.eq("report_type", reportType);
        }
        if (StringUtils.hasText(startDate)) {
            query.ge("start_date", startDate);
        }
        if (StringUtils.hasText(endDate)) {
            query.le("end_date", endDate);
        }
        query.orderByDesc("create_time").last("LIMIT 20");
        return ApiResponse.success(biReviewReportService.list(query));
    }

    /**
     * 权限校验：演示模式无 Token 放通；有 Token 时要求 role_id 为 1（高层）或 2（中层）
     */
    private boolean canAccess(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return true; // 演示模式放通
        }
        try {
            Claims claims = JwtUtils.parseToken(token.substring(7));
            Integer roleId = claims.get("roleId", Integer.class);
            return roleId != null && roleId <= 2;
        } catch (Exception e) {
            return false;
        }
    }

    private String operatorName(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            try {
                Claims claims = JwtUtils.parseToken(token.substring(7));
                return claims.get("username", String.class);
            } catch (Exception ignored) {
            }
        }
        return "admin";
    }
}

@Data
class GenerateReportRequest {
    /** REPORT_TYPE: REVIEW 复盘报告 / ADVICE 决策建议 */
    private String reportType = BiReviewReport.TYPE_REVIEW;
    private String startDate;
    private String endDate;
}
