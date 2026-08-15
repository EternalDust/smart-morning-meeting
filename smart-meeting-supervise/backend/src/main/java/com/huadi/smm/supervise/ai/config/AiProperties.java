package com.huadi.smm.supervise.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {
    private String provider = "deepseek";
    private String baseUrl = "https://api.deepseek.com";
    private String model = "deepseek-chat";
    private String apiKey = "";
    private int timeout = 30000;
}
