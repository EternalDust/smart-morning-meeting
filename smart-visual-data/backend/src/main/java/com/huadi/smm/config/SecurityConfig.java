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
            .csrf(csrf -> csrf.disable()) // 前后端分离通常关闭CSRF，使用Token
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll() // 登录接口放行
                .requestMatchers("/api/dashboard/high-level/**").hasRole("HIGH_LEVEL") // 高层决策人员权限
                .requestMatchers("/api/dashboard/mid-level/**").hasAnyRole("MID_LEVEL", "HIGH_LEVEL") // 中层管理人员权限
                .requestMatchers("/api/dashboard/base-level/**").hasAnyRole("BASE_LEVEL", "MID_LEVEL", "HIGH_LEVEL") // 基层操作人员权限
                .requestMatchers("/ws/**").permitAll() // WebSocket端点放行，实际中可在此处或拦截器加鉴权
                .anyRequest().authenticated()
            );
            
        return http.build();
    }
}
