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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
        String fileUrl = uploadAndGetUrl(audio, fileName);
        String taskId = submitTask(fileUrl);
        String text = pollTask(taskId);
        AsrResult result = new AsrResult();
        result.setText(text);
        result.setDuration(-1);
        return result;
    }

    // 1. 上传音频到 DashScope 文件接口，再查详情拿到可公开访问的临时 URL
    private String uploadAndGetUrl(byte[] audio, String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(apiKey);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource resource = new ByteArrayResource(audio) {
            @Override
            public String getFilename() {
                return fileName == null ? "audio.wav" : fileName;
            }
        };
        body.add("file", resource);
        body.add("purpose", "file-extract");

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
        String uploadResp = restTemplate.postForObject(props.getBaseUrl() + "/api/v1/files", entity, String.class);
        try {
            JsonNode root = objectMapper.readTree(uploadResp);
            JsonNode files = root.path("data").path("uploaded_files");
            if (files.isEmpty()) {
                throw new RuntimeException("音频文件上传失败");
            }
            String fileId = files.get(0).path("file_id").asText("");

            HttpHeaders detailHeaders = new HttpHeaders();
            detailHeaders.setBearerAuth(apiKey);
            HttpEntity<Void> detailEntity = new HttpEntity<>(detailHeaders);
            String detailResp = restTemplate.exchange(props.getBaseUrl() + "/api/v1/files/" + fileId,
                    HttpMethod.GET, detailEntity, String.class).getBody();
            JsonNode detail = objectMapper.readTree(detailResp);
            String url = detail.path("data").path("url").asText("");
            if (url.isEmpty()) {
                throw new RuntimeException("获取音频 URL 失败");
            }
            return url;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("上传音频文件失败", e);
        }
    }

    // 2. 提交异步转写任务（file_urls 方式）
    private String submitTask(String fileUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.add("X-DashScope-Async", "enable");

        try {
            Map<String, Object> input = new HashMap<>();
            input.put("file_urls", Collections.singletonList(fileUrl));
            Map<String, Object> req = new HashMap<>();
            req.put("model", props.getModel());
            req.put("input", input);
            String reqBody = objectMapper.writeValueAsString(req);

            HttpEntity<String> entity = new HttpEntity<>(reqBody, headers);
            String response = restTemplate.postForObject(
                    props.getBaseUrl() + "/api/v1/services/audio/asr/transcription", entity, String.class);
            JsonNode root = objectMapper.readTree(response);
            String taskId = root.path("output").path("task_id").asText("");
            if (taskId.isEmpty()) {
                throw new RuntimeException("语音转写任务提交失败：" + response);
            }
            return taskId;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("提交语音转写任务失败", e);
        }
    }

    // 3. 轮询任务，成功后下载转写结果
    private String pollTask(String taskId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String url = props.getBaseUrl() + "/api/v1/tasks/" + taskId;
        long deadline = System.currentTimeMillis() + 60000;
        try {
            while (System.currentTimeMillis() < deadline) {
                Thread.sleep(2000);
                String response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
                JsonNode root = objectMapper.readTree(response);
                String status = root.path("output").path("task_status").asText("");
                if ("SUCCEEDED".equals(status)) {
                    String transcriptionUrl = root.path("output").path("results")
                            .get(0).path("transcription_url").asText("");
                    if (transcriptionUrl.isEmpty()) {
                        throw new RuntimeException("转写结果为空");
                    }
                    return downloadText(transcriptionUrl);
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

    // 4. 下载转写结果 JSON（签名 URL 需原样请求，不能走 RestTemplate 的 URL 重编码），提取文本
    private String downloadText(String transcriptionUrl) {
        java.net.HttpURLConnection conn = null;
        try {
            java.net.URL url = new java.net.URL(transcriptionUrl);
            conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);
            int code = conn.getResponseCode();
            java.io.InputStream in = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) {
                baos.write(buf, 0, n);
            }
            in.close();
            String response = new String(baos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
            if (code >= 400) {
                throw new RuntimeException("转写结果下载失败: HTTP " + code);
            }
            JsonNode root = objectMapper.readTree(response);
            StringBuilder sb = new StringBuilder();
            for (JsonNode t : root.path("transcripts")) {
                sb.append(t.path("text").asText(""));
            }
            if (sb.length() == 0) {
                throw new RuntimeException("转写结果文本为空");
            }
            return sb.toString();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("下载转写结果失败", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
