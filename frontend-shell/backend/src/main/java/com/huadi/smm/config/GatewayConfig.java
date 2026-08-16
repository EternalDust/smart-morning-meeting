package com.huadi.smm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Configuration
public class GatewayConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) {
            }
        });
        return restTemplate;
    }

    @Bean
    public Map<String, String> subsystemRoutes() {
        return Map.of(
            "report", "http://127.0.0.1:8081",
            "approval", "http://127.0.0.1:8082",
            "supervise", "http://127.0.0.1:8084",
            "visual", "http://127.0.0.1:8086",
            "collection", "http://127.0.0.1:8083"
        );
    }
}
