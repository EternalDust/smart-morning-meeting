package com.huadi.smm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching // 开启Redis缓存
@EnableAsync   // 开启异步任务支持，用于配合外部的大数据计算调度
@EnableScheduling // 开启定时任务，用于模拟WebSocket数据推送
public class SmartMorningMeetingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartMorningMeetingApplication.class, args);
    }
}
