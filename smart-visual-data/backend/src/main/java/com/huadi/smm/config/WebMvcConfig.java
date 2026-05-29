package com.huadi.smm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 演示模式：放通所有API
        // registry.addInterceptor(jwtInterceptor)
        //         .addPathPatterns("/api/dashboard/**")
        //         .excludePathPatterns("/api/auth/login", "/ws/**", "/api/dashboard/test-insert");
    }
}