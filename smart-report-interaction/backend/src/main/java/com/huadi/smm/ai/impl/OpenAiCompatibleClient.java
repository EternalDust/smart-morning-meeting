package com.huadi.smm.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huadi.smm.ai.AiClient;
import com.huadi.smm.ai.config.AiProperties;
import com.huadi.smm.ai.dto.SpeechLine;
import com.huadi.smm.ai.dto.SummaryResult;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenAiCompatibleClient implements AiClient {

    private final AiProperties props;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiCompatibleClient(AiProperties props, RestTemplate restTemplate) {
        this.props = props;
        this.restTemplate = restTemplate;
    }

    @Override
    public SummaryResult generateSummary(String meetingTitle, List<SpeechLine> speeches) {
        String systemPrompt = "你是医院晨会记录助手，根据用户提供的晨会发言记录生成结构化会议摘要。" +
                "必须只输出一个 JSON 对象，不要输出任何其他文字或代码块，格式如下：" +
                "{\"summary\":\"一段完整的会议摘要\",\"keyPoints\":[\"要点1\",\"要点2\"],\"decisions\":[\"决策或待办\"],\"medicalEntities\":[\"医疗实体\"]}。" +
                "规则：summary 概括各发言人汇报重点并保持连贯；keyPoints 提炼3-6条关键要点；" +
                "decisions 提取需要跟进的决策或待办事项，没有则为空数组；" +
                "medicalEntities 从发言中提取医疗实体（疾病、症状、药品、检查项目、科室等），去重。";
        String content = chat(systemPrompt, buildSpeechesText(meetingTitle, speeches), true);
        return parseSummary(content);
    }

    @Override
    public String answerQuestion(String question, List<SpeechLine> context) {
        String systemPrompt = "你是医院晨会助手，根据晨会发言上下文回答主持人的问题，给出初步答复。" +
                "要求：简洁、可操作、符合医疗行业规范；如果上下文中没有相关信息，如实说明并结合医疗常识给出通用建议。";
        String userPrompt = "发言上下文：\n" + buildSpeechesText("", context) +
                "\n\n主持人问题：" + (question == null ? "" : question) + "\n请给出初步答复。";
        return chat(systemPrompt, userPrompt, false);
    }

    private String chat(String systemPrompt, String userPrompt, boolean jsonMode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(props.getApiKey().trim());

        Map<String, Object> body = new HashMap<>();
        body.put("model", props.getModel());
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(msg("system", systemPrompt));
        messages.add(msg("user", userPrompt));
        body.put("messages", messages);
        body.put("temperature", 0.3);
        if (jsonMode) {
            Map<String, String> responseFormat = new HashMap<>();
            responseFormat.put("type", "json_object");
            body.put("response_format", responseFormat);
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        String url = props.getBaseUrl() + "/chat/completions";
        String responseBody = restTemplate.postForObject(url, entity, String.class);
        if (responseBody == null) {
            throw new RuntimeException("大模型无响应");
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            if (content.isEmpty()) {
                throw new RuntimeException("大模型返回内容为空");
            }
            return stripCodeFence(content);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("解析大模型响应失败: " + e.getMessage(), e);
        }
    }

    private Map<String, String> msg(String role, String content) {
        Map<String, String> m = new HashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }

    private SummaryResult parseSummary(String content) {
        try {
            return objectMapper.readValue(content, SummaryResult.class);
        } catch (Exception e) {
            SummaryResult fallback = new SummaryResult();
            fallback.setSummary(content);
            return fallback;
        }
    }

    private String stripCodeFence(String content) {
        String text = content.trim();
        if (text.startsWith("```")) {
            int firstNewline = text.indexOf('\n');
            if (firstNewline > 0) {
                text = text.substring(firstNewline + 1);
            }
            if (text.endsWith("```")) {
                text = text.substring(0, text.length() - 3);
            }
        }
        return text.trim();
    }

    private String buildSpeechesText(String meetingTitle, List<SpeechLine> speeches) {
        StringBuilder sb = new StringBuilder();
        sb.append("会议主题：").append(meetingTitle == null || meetingTitle.isEmpty() ? "未填写" : meetingTitle).append("\n");
        sb.append("发言记录：\n");
        if (speeches != null) {
            for (int i = 0; i < speeches.size(); i++) {
                SpeechLine s = speeches.get(i);
                sb.append(i + 1).append(". ").append(s.getSpeaker()).append("：")
                        .append(s.getContent() == null ? "" : s.getContent()).append("\n");
            }
        }
        return sb.toString();
    }
}
