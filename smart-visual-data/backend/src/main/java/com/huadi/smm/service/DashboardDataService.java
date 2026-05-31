package com.huadi.smm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huadi.smm.entity.BiStatSupervise;
import com.huadi.smm.mapper.BiStatSuperviseMapper;
import com.huadi.smm.websocket.MorningMeetingDataHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardDataService {

    private final MorningMeetingDataHandler webSocketHandler;
    private final BiStatSuperviseMapper biStatSuperviseMapper;
    private final Random random = new Random();

    // 问题解决率：从数据库汇总，不实时浮动
    private volatile double resolutionRate = 90.0;

    @PostConstruct
    public void init() {
        refreshResolutionRate();
    }

    /**
     * 从数据库汇总真实解决率（每5分钟刷新一次）
     */
    @Scheduled(fixedRate = 300_000)
    public void refreshResolutionRate() {
        try {
            List<BiStatSupervise> list = biStatSuperviseMapper.selectList(
                new LambdaQueryWrapper<BiStatSupervise>()
                    .select(BiStatSupervise::getSolveRate)
                    .isNotNull(BiStatSupervise::getSolveRate)
            );
            if (!list.isEmpty()) {
                double avg = list.stream()
                    .map(BiStatSupervise::getSolveRate)
                    .mapToDouble(BigDecimal::doubleValue)
                    .average()
                    .orElse(90.0);
                resolutionRate = BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP).doubleValue();
                log.info("已从数据库刷新问题解决率: {}%", resolutionRate);
            }
        } catch (Exception e) {
            log.warn("查询解决率失败，使用上次缓存值: {}%", resolutionRate);
        }
    }

    /**
     * 模拟实时数据推送（每3秒）：
     * - 参会率：实时浮动，模拟晨会进行中人员进出
     * - 问题解决率：从数据库汇总的固定值，不随意变动
     * - 风险级别：稳定，仅当参会率过低时预警
     */
    @Scheduled(fixedRate = 3000)
    public void syncDataToDashboard() {
        // 参会率：在基础值附近小幅波动
        double attendanceRate = 85 + random.nextDouble() * 15;

        // 风险级别：参会率低于75%才预警，否则正常
        String riskLevel = attendanceRate < 75 ? "WARNING" : "NORMAL";

        String json = String.format(
            "{\"attendanceRate\": %.1f, \"resolutionRate\": %.1f, \"riskLevel\": \"%s\", \"timestamp\": \"%s\"}",
            attendanceRate, resolutionRate, riskLevel, LocalDateTime.now()
        );

        webSocketHandler.pushRealTimeDataToDashboard(json);
    }
}
