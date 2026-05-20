package com.huadi.smm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.huadi.smm")
@MapperScan("com.huadi.smm.**.dao")
public class SmartDataCollectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartDataCollectionApplication.class, args);
    }
}
