package com.huadi.smm.ai.config;

import com.huadi.smm.ai.FaceClient;
import com.huadi.smm.ai.impl.DashScopeFaceClient;
import com.huadi.smm.ai.impl.MockFaceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class FaceConfig {

    @Bean
    public FaceClient faceClient(FaceProperties props, AiProperties aiProps, RestTemplate aiRestTemplate) {
        String key = resolveKey(props.getApiKey(), aiProps.getApiKey());
        if (key != null && !key.trim().isEmpty()) {
            return new DashScopeFaceClient(props, aiRestTemplate, key);
        }
        return new MockFaceClient();
    }

    private String resolveKey(String specific, String fallback) {
        if (specific != null && !specific.trim().isEmpty()) {
            return specific;
        }
        return fallback;
    }
}
