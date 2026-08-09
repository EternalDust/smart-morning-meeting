package com.huadi.smm.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huadi.smm.ai.FaceClient;
import com.huadi.smm.ai.config.FaceProperties;
import com.huadi.smm.ai.dto.FaceResult;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashScopeFaceClient implements FaceClient {

    private final FaceProperties props;
    private final RestTemplate restTemplate;
    private final String apiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DashScopeFaceClient(FaceProperties props, RestTemplate restTemplate, String apiKey) {
        this.props = props;
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    @Override
    public FaceResult recognize(byte[] image, String fileName, String expectedUserId) {
        String dataUrl = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(image);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", props.getModel());

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        List<Map<String, Object>> content = new ArrayList<>();
        Map<String, Object> imagePart = new HashMap<>();
        imagePart.put("type", "image_url");
        Map<String, Object> imageUrl = new HashMap<>();
        imageUrl.put("url", dataUrl);
        imagePart.put("image_url", imageUrl);
        content.add(imagePart);
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("type", "text");
        textPart.put("text", "请分析这张照片：1) 是否包含清晰的人脸；2) 是否与登录用户（工号 " + (expectedUserId == null ? "未知" : expectedUserId) + "）的样貌吻合。只输出 吻合/不吻合 和一句简要说明。");
        content.add(textPart);
        message.put("content", content);
        body.put("messages", Collections.singletonList(message));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        String response = restTemplate.postForObject(props.getBaseUrl() + "/chat/completions", entity, String.class);
        try {
            JsonNode root = objectMapper.readTree(response);
            String description = root.path("choices").path(0).path("message").path("content").asText("");
            FaceResult result = new FaceResult();
            result.setMatched(false);
            result.setMessage(description.isEmpty() ? "未识别到人脸" : description);
            return result;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("解析人脸识别响应失败", e);
        }
    }
}
