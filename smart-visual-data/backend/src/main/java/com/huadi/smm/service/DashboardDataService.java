package com.huadi.smm.service;

import com.huadi.smm.websocket.MorningMeetingDataHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DashboardDataService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MorningMeetingDataHandler webSocketHandler;

    /**
     * 设计文档要求：Celery异步调度、Spark分布式计算的结果，可写入Redis缓冲。
     * 此处模拟：后端定时从Redis读取Spark写入的最新核心指标（或接收消息队列回调），并通过WebSocket推送到前端大屏。
     */
    // @Scheduled(fixedRate = 3000) // 模拟每3秒秒级更新，需主类开启 @EnableScheduling
    public void syncDataToDashboard() {
        // 1. 尝试从Redis中获取最新的参会率和问题解决率 (模拟Spark存入Redis的键值)
        Object realTimeMetrics = redisTemplate.opsForValue().get("spark:metrics:morning_meeting:realtime");
        
        if (realTimeMetrics == null) {
            // 模拟数据结构
            realTimeMetrics = "{\"attendanceRate\": 98.5, \"resolutionRate\": 92.3, \"riskLevel\": \"NORMAL\", \"timestamp\": \"" + LocalDateTime.now() + "\"}";
        }

        // 2. 通过WebSocket推送给前端大屏
        webSocketHandler.pushRealTimeDataToDashboard(realTimeMetrics.toString());
    }
}
