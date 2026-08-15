package com.huadi.smm.ai.config;

import com.huadi.smm.ai.AsrClient;
import com.huadi.smm.ai.impl.DashScopeAsrClient;
import com.huadi.smm.ai.impl.MockAsrClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AsrConfig {

    @Bean
    public AsrClient asrClient(AsrProperties props, AiProperties aiProps, RestTemplate aiRestTemplate) {
        String key = resolveKey(props.getApiKey(), aiProps.getApiKey());
        if (key != null && !key.trim().isEmpty()) {
            return new DashScopeAsrClient(props, aiRestTemplate, key);
        }
        return new MockAsrClient();
    }

    private String resolveKey(String specific, String fallback) {
        if (specific != null && !specific.trim().isEmpty()) {
            return specific;
        }
        return fallback;
    }
}
