package com.huadi.smm.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huadi.smm.ai.AsrClient;
import com.huadi.smm.ai.config.AsrProperties;
import com.huadi.smm.ai.dto.AsrResult;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class DashScopeAsrClient implements AsrClient {

    private final AsrProperties props;
    private final RestTemplate restTemplate;
    private final String apiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DashScopeAsrClient(AsrProperties props, RestTemplate restTemplate, String apiKey) {
        this.props = props;
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    @Override
    public AsrResult transcribe(byte[] audio, String fileName) {
        String taskId = submitTask(audio, fileName);
        String text = pollTask(taskId);
        AsrResult result = new AsrResult();
        result.setText(text);
        result.setDuration(-1);
        return result;
    }

    private String submitTask(byte[] audio, String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(apiKey);
        headers.add("X-DashScope-Async", "enable");

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("model", props.getModel());
        ByteArrayResource resource = new ByteArrayResource(audio) {
            @Override
            public String getFilename() {
                return fileName == null ? "audio.wav" : fileName;
            }
        };
        body.add("file", resource);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
        String url = props.getBaseUrl() + "/api/v1/services/audio/asr/transcription";
        String response = restTemplate.postForObject(url, entity, String.class);
        try {
            JsonNode root = objectMapper.readTree(response);
            String taskId = root.path("output").path("task_id").asText("");
            if (taskId.isEmpty()) {
                throw new RuntimeException("语音转写任务提交失败");
            }
            return taskId;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("解析语音转写任务响应失败", e);
        }
    }

    private String pollTask(String taskId) {
        String url = props.getBaseUrl() + "/api/v1/services/audio/asr/transcription/" + taskId;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        long deadline = System.currentTimeMillis() + 60000;
        try {
            while (System.currentTimeMillis() < deadline) {
                Thread.sleep(2000);
                String response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
                JsonNode root = objectMapper.readTree(response);
                String status = root.path("output").path("task_status").asText("");
                if ("SUCCEEDED".equals(status)) {
                    StringBuilder sb = new StringBuilder();
                    for (JsonNode sentence : root.path("output").path("result").path("transcription").path("sentences")) {
                        sb.append(sentence.path("text").asText(""));
                    }
                    if (sb.length() == 0) {
                        throw new RuntimeException("语音转写结果为空");
                    }
                    return sb.toString();
                }
                if ("FAILED".equals(status)) {
                    throw new RuntimeException("语音转写任务失败");
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("语音转写任务查询失败", e);
        }
        throw new RuntimeException("语音转写任务超时");
    }
}
