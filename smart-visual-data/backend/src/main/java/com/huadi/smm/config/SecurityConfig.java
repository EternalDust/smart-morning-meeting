package com.huadi.smm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 开启基于方法的权限控制 (如 @PreAuthorize)
public class SecurityConfig {

    /**
     * 设计文档要求：实现精细化的权限分级管理，为基层操作人员、中层管理人员、高层决策人员分配不同的角色权限
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configure(http)) // 允许跨域
            .csrf(csrf -> csrf.disable()) // 前后端分离通常关闭CSRF，使用Token
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll() // 登录接口放行
                .requestMatchers("/api/dashboard/trend").permitAll() // 图表接口放行
                .requestMatchers("/api/dashboard/issues-distribution").permitAll() // 图表接口放行
                .requestMatchers("/api/dashboard/test-insert").permitAll() // 临时放行测试接口
                .requestMatchers("/api/dashboard/base-level/**").permitAll() // 临时放行基础数据接口以便测试
                .requestMatchers("/api/dashboard/high-level/**").hasRole("HIGH_LEVEL") // 高层决策人员权限
                .requestMatchers("/api/dashboard/mid-level/**").hasAnyRole("MID_LEVEL", "HIGH_LEVEL") // 中层管理人员权限
                .requestMatchers("/ws/**").permitAll() // WebSocket端点放行，实际中可在此处或拦截器加鉴权
                .anyRequest().authenticated()
            );
            
        return http.build();
    }
}
