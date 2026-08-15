package com.huadi.smm.supervise.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huadi.smm.supervise.ai.AiClient;
import com.huadi.smm.supervise.ai.config.AiProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI 兼容接口的真实大模型实现（默认 DeepSeek）。
 */
public class OpenAiCompatibleClient implements AiClient {

    private final AiProperties props;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiCompatibleClient(AiProperties props, RestTemplate restTemplate) {
        this.props = props;
        this.restTemplate = restTemplate;
    }

    @Override
    public String chat(String systemPrompt, String userPrompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(props.getApiKey().trim());

            Map<String, Object> body = new HashMap<>();
            body.put("model", props.getModel());
            body.put("temperature", 0.6);
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(msg("system", systemPrompt));
            messages.add(msg("user", userPrompt));
            body.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            String url = props.getBaseUrl() + (props.getBaseUrl().endsWith("/") ? "" : "/") + "chat/completions";
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                return null;
            }
            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").path(0).path("message").path("content").asText(null);
            return (content == null || content.isBlank()) ? null : content.trim();
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, String> msg(String role, String content) {
        Map<String, String> m = new HashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }
}
