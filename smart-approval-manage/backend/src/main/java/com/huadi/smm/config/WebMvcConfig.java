package com.huadi.smm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    //@Autowired
    //private JwtInterceptor jwtInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    //@Override
    //public void addInterceptors(InterceptorRegistry registry) {
        // 演示模式：放通所有API
        // registry.addInterceptor(jwtInterceptor)
        //         .addPathPatterns("/api/agenda/**")
        //         .excludePathPatterns("/api/agenda/public/**", "/api/auth/**");
    //}
}