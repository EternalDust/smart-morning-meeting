package com.huadi.smm.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.huadi.smm.dao")
public class MybatisPlusConfig {
}