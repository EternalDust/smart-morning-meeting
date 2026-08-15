package com.huadi.smm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SmartApprovalManageApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartApprovalManageApplication.class, args);
    }
}