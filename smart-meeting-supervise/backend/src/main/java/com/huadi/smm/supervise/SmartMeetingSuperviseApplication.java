package com.huadi.smm.supervise;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.huadi.smm.supervise.mapper")
public class SmartMeetingSuperviseApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartMeetingSuperviseApplication.class, args);
    }
}