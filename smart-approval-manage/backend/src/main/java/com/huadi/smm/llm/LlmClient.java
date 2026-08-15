package com.huadi.smm.llm;

import com.huadi.smm.config.LlmProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LlmClient {

    @Autowired
    private LlmProperties props;

    @Autowired
    private RestTemplate restTemplate;

    public String chat(String systemPrompt, String userPrompt) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", props.getModel());
        body.put("temperature", props.getTemperature());

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(makeMsg("system", systemPrompt));
        messages.add(makeMsg("user", userPrompt));
        body.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(props.getApiKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> resp = restTemplate.postForEntity(props.getApiUrl(), entity, Map.class);

        return extractContent(resp.getBody());
    }

    private Map<String, String> makeMsg(String role, String content) {
        Map<String, String> m = new HashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }

    private String extractContent(Map<?, ?> respBody) {
        if (respBody == null) return "";
        Object choicesObj = respBody.get("choices");
        if (!(choicesObj instanceof List)) return "";
        List<?> choices = (List<?>) choicesObj;
        if (choices.isEmpty()) return "";
        Object first = choices.get(0);
        if (!(first instanceof Map)) return "";
        Object msgObj = ((Map<?, ?>) first).get("message");
        if (!(msgObj instanceof Map)) return "";
        Object content = ((Map<?, ?>) msgObj).get("content");
        return content != null ? content.toString() : "";
    }
}