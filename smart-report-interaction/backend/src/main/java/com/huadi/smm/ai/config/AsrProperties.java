package com.huadi.smm.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "asr")
public class AsrProperties {
    private String apiKey = "";
    private String baseUrl = "https://dashscope.aliyuncs.com";
    private String model = "paraformer-v2";
}
