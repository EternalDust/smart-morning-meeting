package com.huadi.smm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "llm")
public class LlmProperties {
    private String apiUrl;
    private String apiKey;
    private String model;
    private Double temperature = 0.7;
}