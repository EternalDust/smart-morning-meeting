package com.huadi.smm.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huadi.smm.config.AiLlmProperties;
import com.huadi.smm.entity.BiReviewReport;
import com.huadi.smm.entity.BiStatMedical;
import com.huadi.smm.entity.BiStatMeeting;
import com.huadi.smm.entity.BiStatSupervise;
import com.huadi.smm.entity.BiWarnRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 大模型复盘报告 / 管理决策建议生成服务
 * 流程：数据提取 → 提示词组装 → 模型推理调用（超时重试≤3）→ 结果校验 → 持久化
 * 未配置大模型服务地址（AI_LLM_BASE_URL 为空）或调用持续失败时，降级为模板模拟生成，
 * 保证演示链路可用，生成状态通过 status 字段标识（1 真实 / 2 模拟）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewReportService {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 预警类型编码 → 中文（与 bigdata/models/config.py WARN_TYPES 保持一致） */
    private static final Map<String, String> WARN_TYPE_TEXT = Map.of(
        "ATTEND_DROP", "参会率骤降",
        "SOLVE_DROP", "问题解决率不达标",
        "OVERDUE_SURGE", "逾期数激增",
        "DURATION_ABNORMAL", "晨会时长异常",
        "COMPLETE_DROP", "业务指标完成度下降",
        "ABSENT_SURGE", "缺席人数异常增多"
    );

    private final BiStatMeetingService biStatMeetingService;
    private final BiStatSuperviseService biStatSuperviseService;
    private final BiStatMedicalService biStatMedicalService;
    private final BiWarnRecordService biWarnRecordService;
    private final BiReviewReportService biReviewReportService;
    private final AiLlmProperties props;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * 生成复盘报告 / 决策建议并持久化
     */
    public BiReviewReport generateReport(String reportType, String startDate, String endDate, String operator) {
        String start = StringUtils.hasText(startDate) ? startDate : LocalDateTime.now().minusDays(6).toLocalDate().toString();
        String end = StringUtils.hasText(endDate) ? endDate : LocalDateTime.now().toLocalDate().toString();

        // 1. 数据提取
        Map<String, Object> metrics = extractMetrics(start, end);

        // 2. 提示词组装
        String prompt = buildPrompt(reportType, metrics);

        // 3. 模型推理调用（失败/校验不通过时重试，最多 maxRetry 次）
        String content = null;
        int status = BiReviewReport.STATUS_FAILED;
        for (int attempt = 0; attempt < props.getMaxRetry(); attempt++) {
            if (!StringUtils.hasText(props.getBaseUrl())) {
                // 未配置大模型地址 → 模板模拟生成
                content = simulateReport(reportType, metrics);
                status = BiReviewReport.STATUS_SIMULATED;
                break;
            }
            try {
                content = callLlm(prompt);
            } catch (Exception e) {
                log.warn("第 {} 次调用大模型失败: {}", attempt + 1, e.getMessage());
                content = null;
                continue;
            }
            // 4. 结果校验（章节完整性 / 关键词合法性 / 长度）
            if (validateReport(reportType, content)) {
                status = BiReviewReport.STATUS_SUCCESS;
                break;
            }
            log.warn("第 {} 次生成结果未通过校验，重新生成", attempt + 1);
            content = null;
        }

        // 重试仍失败 → 降级为模板模拟生成，避免演示链路中断
        if (content == null) {
            log.warn("大模型生成持续失败，降级为模板模拟生成");
            content = simulateReport(reportType, metrics);
            status = BiReviewReport.STATUS_SIMULATED;
        }

        // 5. 结果持久化
        BiReviewReport report = new BiReviewReport();
        report.setTitle(BiReviewReport.TYPE_ADVICE.equals(reportType) ? "管理决策建议" : "晨会复盘报告");
        report.setContent(content);
        report.setReportType(reportType);
        report.setStartDate(start);
        report.setEndDate(end);
        report.setStatus(status);
        report.setCreateBy(StringUtils.hasText(operator) ? operator : "admin");
        report.setCreateTime(LocalDateTime.now().format(DTF));
        biReviewReportService.save(report);
        return report;
    }

    /**
     * 数据提取：从数据仓库事实表聚合核心指标（参会率、解决率、业务完成度、趋势、预警记录）
     */
    private Map<String, Object> extractMetrics(String startDate, String endDate) {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("period", startDate + " 至 " + endDate);

        // 参会率趋势
        List<BiStatMeeting> meetings = biStatMeetingService.list(
            new QueryWrapper<BiStatMeeting>()
                .between("stat_date", startDate, endDate)
                .orderByAsc("stat_date"));
        double attendRate = meetings.stream()
            .map(BiStatMeeting::getAttendRate).filter(java.util.Objects::nonNull)
            .mapToDouble(BigDecimal::doubleValue).average().orElse(0);
        metrics.put("attend_rate", round1(attendRate));

        // 问题解决率趋势
        List<BiStatSupervise> supervises = biStatSuperviseService.list(
            new QueryWrapper<BiStatSupervise>()
                .between("stat_date", startDate, endDate)
                .orderByAsc("stat_date"));
        double solveRate = supervises.stream()
            .map(BiStatSupervise::getSolveRate).filter(java.util.Objects::nonNull)
            .mapToDouble(BigDecimal::doubleValue).average().orElse(0);
        metrics.put("solve_rate", round1(solveRate));

        // 业务指标完成度
        double completeRate = biStatMedicalService.list(
            new QueryWrapper<BiStatMedical>().between("stat_date", startDate, endDate)).stream()
            .map(BiStatMedical::getCompleteRate).filter(java.util.Objects::nonNull)
            .mapToDouble(BigDecimal::doubleValue).average().orElse(0);
        metrics.put("business_completion_rate", round1(completeRate));

        // 趋势描述
        Map<String, String> trend = new LinkedHashMap<>();
        trend.put("attend_rate_trend", describeTrend(first(meetings), last(meetings)));
        trend.put("solve_rate_trend", describeTrend(firstSupervise(supervises), lastSupervise(supervises)));
        metrics.put("trend", trend);

        // 周期内最新预警记录
        List<BiWarnRecord> warns = biWarnRecordService.list(
            new QueryWrapper<BiWarnRecord>()
                .ge("create_time", startDate + " 00:00:00")
                .le("create_time", endDate + " 23:59:59")
                .orderByDesc("create_time")
                .last("LIMIT 5"));
        List<Map<String, Object>> warnList = new ArrayList<>();
        for (BiWarnRecord w : warns) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("dept", "科室 " + (w.getDeptId() == null ? "-" : w.getDeptId()));
            m.put("type", WARN_TYPE_TEXT.getOrDefault(w.getWarnType(), w.getWarnType()));
            m.put("level", w.getWarnLevel());
            m.put("value", w.getAbnormalValue());
            warnList.add(m);
        }
        metrics.put("warnings", warnList);

        // 总体概述
        metrics.put("summary", buildSummary(round1(attendRate), round1(solveRate), round1(completeRate), warnList.size()));

        return metrics;
    }

    private double first(List<BiStatMeeting> list) {
        return list.isEmpty() || list.get(0).getAttendRate() == null ? 0 : list.get(0).getAttendRate().doubleValue();
    }

    private double last(List<BiStatMeeting> list) {
        return list.isEmpty() || list.get(list.size() - 1).getAttendRate() == null ? 0 : list.get(list.size() - 1).getAttendRate().doubleValue();
    }

    private double firstSupervise(List<BiStatSupervise> list) {
        return list.isEmpty() || list.get(0).getSolveRate() == null ? 0 : list.get(0).getSolveRate().doubleValue();
    }

    private double lastSupervise(List<BiStatSupervise> list) {
        return list.isEmpty() || list.get(list.size() - 1).getSolveRate() == null ? 0 : list.get(list.size() - 1).getSolveRate().doubleValue();
    }

    private String describeTrend(double firstVal, double lastVal) {
        if (lastVal - firstVal > 0.5) return "上升";
        if (lastVal - firstVal < -0.5) return "下降";
        return "稳定";
    }

    private double round1(double v) {
        return BigDecimal.valueOf(v).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private String buildSummary(double attend, double solve, double complete, int warnCount) {
        String part = attend >= 90 ? "参会情况良好" : (attend >= 85 ? "参会情况基本达标" : "参会率偏低");
        String part2 = warnCount == 0 ? "周期内无异常预警" : "存在 " + warnCount + " 条异常预警需重点关注";
        return "本周全院晨会运行总体平稳，" + part + "，问题解决率 " + solve + "%，业务指标完成度 " + complete + "%，" + part2;
    }

    /**
     * 提示词组装：系统指令 + 指标数据（JSON）
     */
    private String buildPrompt(String reportType, Map<String, Object> metrics) {
        String dataJson;
        try {
            dataJson = objectMapper.writeValueAsString(metrics);
        } catch (Exception e) {
            dataJson = metrics.toString();
        }

        if (BiReviewReport.TYPE_ADVICE.equals(reportType)) {
            return "你是一名医疗企业晨会管理分析专家。请根据以下晨会核心指标与风险预警数据，"
                + "生成一份管理决策建议，要求包含四部分：\n"
                + "一、总体态势研判；\n"
                + "二、分级处置策略（依据预警等级明确处置优先级、责任科室与时限要求）；\n"
                + "三、资源配置建议；\n"
                + "四、近期重点跟进事项。\n"
                + "要求建议可落地、责任到科室、时限明确，全文不少于600字。\n\n"
                + "【指标数据】\n" + dataJson;
        }
        return "你是一名医疗企业晨会管理分析专家。请根据以下晨会核心指标数据，"
            + "生成一份结构化晨会复盘报告，要求包含五个部分：\n"
            + "一、晨会概况（整体运行情况概述）；\n"
            + "二、核心指标完成情况（参会率、问题解决率、业务指标完成度及变化趋势）；\n"
            + "三、异常与风险分析（依据预警记录逐条分析异常原因与影响）；\n"
            + "四、存在的问题（归纳当前晨会管理中的共性问题）；\n"
            + "五、改进建议（给出可落地的管理优化建议）。\n"
            + "要求语言客观严谨，数据引用准确，全文不少于800字。\n\n"
            + "【指标数据】\n" + dataJson;
    }

    /**
     * 模型推理调用：请求 OpenAI 兼容的 chat/completions 接口，带超时
     */
    private String callLlm(String prompt) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("model", props.getModel());
        body.put("temperature", props.getTemperature());
        body.put("max_tokens", props.getMaxTokens());
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(props.getBaseUrl() + "/chat/completions"))
                .timeout(Duration.ofSeconds(props.getReadTimeoutSeconds()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + props.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("大模型接口返回状态码 " + response.statusCode());
        }
        JsonNode root = objectMapper.readTree(response.body());
        JsonNode content = root.path("choices").get(0).path("message").path("content");
        return content == null ? "" : content.asText("");
    }

    /**
     * 结果校验：章节完整性 + 关键词合法性 + 基本长度
     */
    private boolean validateReport(String reportType, String content) {
        if (content == null || content.trim().length() < 100) {
            return false;
        }
        String[] markers = BiReviewReport.TYPE_ADVICE.equals(reportType)
            ? new String[]{"研判", "处置", "资源", "跟进", "建议"}
            : new String[]{"概况", "完成情况", "风险", "问题", "建议"};
        int hit = 0;
        for (String m : markers) {
            if (content.contains(m)) {
                hit++;
            }
        }
        return hit >= 3;
    }

    /**
     * 模板模拟生成：基于真实聚合指标构建结构化 Markdown 报告
     */
    private String simulateReport(String reportType, Map<String, Object> metrics) {
        double attend = ((Number) metrics.get("attend_rate")).doubleValue();
        double solve = ((Number) metrics.get("solve_rate")).doubleValue();
        double complete = ((Number) metrics.get("business_completion_rate")).doubleValue();
        @SuppressWarnings("unchecked")
        Map<String, String> trend = (Map<String, String>) metrics.get("trend");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> warns = (List<Map<String, Object>>) metrics.get("warnings");
        String period = (String) metrics.get("period");

        if (BiReviewReport.TYPE_ADVICE.equals(reportType)) {
            StringBuilder sb = new StringBuilder();
            sb.append("# 管理决策建议\n\n");
            sb.append("**统计周期：** ").append(period).append("\n\n");
            sb.append("## 一、总体态势研判\n\n");
            sb.append("本期全院晨会运行总体平稳：平均参会率 ").append(attend).append("%，问题解决率 ")
              .append(solve).append("%，业务指标完成度 ").append(complete).append("%。参会率呈")
              .append(trend.get("attend_rate_trend")).append("趋势，问题解决率").append(trend.get("solve_rate_trend"))
              .append("。").append(warns.isEmpty() ? "周期内无重大异常。" : "存在 " + warns.size() + " 条预警记录，需分级处置。").append("\n\n");
            sb.append("## 二、分级处置策略\n\n");
            if (warns.isEmpty()) {
                sb.append("1. 维持常规晨会运行节奏，保持指标监控频度不变；\n");
                sb.append("2. 每场晨会核对参会率与问题销项进度，及时预警。\n\n");
            } else {
                int i = 1;
                for (Map<String, Object> w : warns) {
                    String levelText = warnLevelText(((Number) w.get("level")).intValue());
                    sb.append(i++).append(". ").append(w.get("dept")).append("「").append(w.get("type"))
                      .append("」（").append(levelText).append("）：24 小时内完成原因排查，明确责任科室与责任人，中层管理人员督办销项；\n");
                }
                sb.append("\n");
            }
            sb.append("## 三、资源配置建议\n\n");
            sb.append("1. 建议向参会率偏低的科室增配晨会提醒与签到辅助工具，降低组织成本；\n");
            sb.append("2. 对高频预警指标配置自动化检测与推送，减少人工盯盘投入。\n\n");
            sb.append("## 四、近期重点跟进事项\n\n");
            sb.append("1. 紧盯参会率异常科室的整改落实情况，按周复核；\n");
            sb.append("2. 完善问题督办超时告警，保证闭环时效。\n");
            return sb.toString();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("# 晨会复盘报告\n\n");
        sb.append("**统计周期：** ").append(period).append("\n\n");
        sb.append("## 一、晨会概况\n\n");
        sb.append("本周全院晨会运行总体平稳，平均参会率 ").append(attend).append("%，问题解决率 ")
          .append(solve).append("%，业务指标完成度 ").append(complete).append("%。").append(metrics.get("summary")).append("\n\n");
        sb.append("## 二、核心指标完成情况\n\n");
        sb.append("- 参会率：").append(attend).append("%（趋势：").append(trend.get("attend_rate_trend")).append("）\n");
        sb.append("- 问题解决率：").append(solve).append("%（趋势：").append(trend.get("solve_rate_trend")).append("）\n");
        sb.append("- 业务指标完成度：").append(complete).append("%\n\n");
        sb.append("## 三、异常与风险分析\n\n");
        if (warns.isEmpty()) {
            sb.append("周期内未检测到异常预警，运行状态正常。\n\n");
        } else {
            int i = 1;
            for (Map<String, Object> w : warns) {
                String levelText = warnLevelText(((Number) w.get("level")).intValue());
                sb.append(i++).append(". ").append(w.get("dept")).append("出现「").append(w.get("type"))
                  .append("」，预警等级：").append(levelText).append("，异常值：").append(w.get("value"))
                  .append("。建议尽快排查原因，评估对晨会组织与督办闭环的影响。\n");
            }
            sb.append("\n");
        }
        sb.append("## 四、存在的问题\n\n");
        if (attend < 85) {
            sb.append("1. 部分科室参会率低于 85%，晨会组织纪律有待加强；\n");
        } else {
            sb.append("1. 整体参会情况良好，个别时段存在参会率波动，需关注人员安排；\n");
        }
        sb.append("2. 预警处置时效仍有提升空间，建议强化限时督办机制。\n\n");
        sb.append("## 五、改进建议\n\n");
        sb.append("1. 对参会率偏低的科室在晨会前发送提醒，明确会议纪律要求；\n");
        sb.append("2. 建立预警分级处置台账，重大预警由中层及以上管理人员直接督办；\n");
        sb.append("3. 每周定期复盘核心指标变化趋势，动态调整管理策略。\n");
        return sb.toString();
    }

    private String warnLevelText(int level) {
        if (level >= 3) return "重大预警";
        if (level == 2) return "中等预警";
        return "一般预警";
    }
}
