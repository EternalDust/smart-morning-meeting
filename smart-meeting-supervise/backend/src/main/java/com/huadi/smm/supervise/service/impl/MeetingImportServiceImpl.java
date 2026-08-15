package com.huadi.smm.supervise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huadi.smm.supervise.entity.Problem;
import com.huadi.smm.supervise.mapper.ProblemMapper;
import com.huadi.smm.supervise.service.MeetingImportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对接汇报交互的 RESTful 读接口，把晨会互动中的提问/反馈和会议摘要导入为督办问题。
 */
@Service
public class MeetingImportServiceImpl implements MeetingImportService {

    private final ProblemMapper problemMapper;
    private final String reportBaseUrl;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MeetingImportServiceImpl(ProblemMapper problemMapper,
                                    @Value("${report.base-url:http://127.0.0.1:8081}") String reportBaseUrl) {
        this.problemMapper = problemMapper;
        this.reportBaseUrl = reportBaseUrl.endsWith("/")
                ? reportBaseUrl.substring(0, reportBaseUrl.length() - 1)
                : reportBaseUrl;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(15000);
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public Map<String, Object> importFromMeeting(Long meetingId) {
        if (meetingId == null) {
            throw new IllegalArgumentException("会议ID不能为空");
        }
        int imported = 0;
        int skipped = 0;
        List<String> messages = new ArrayList<>();

        // 1. 互动消息：1=提问 2=反馈，都作为问题来源
        for (int type : new int[]{1, 2}) {
            JsonNode data = getData("/api/meeting/interaction/list/" + meetingId + "?type=" + type);
            if (data == null) {
                continue;
            }
            JsonNode arr = data.path("messages");
            JsonNode nameMap = data.path("nameMap");
            if (!arr.isArray()) {
                continue;
            }
            for (JsonNode node : arr) {
                String content = node.path("content").asText("");
                if (content.isBlank()) {
                    continue;
                }
                String typeName = type == 1 ? "提问" : "反馈";
                String userId = node.path("userId").asText("");
                String speaker = nameMap.path(userId).asText(userId);
                String title = content.length() > 200 ? content.substring(0, 200) : content;
                if (exists(meetingId, title)) {
                    skipped++;
                    continue;
                }
                Problem p = new Problem();
                p.setMeetingId(meetingId);
                p.setTitle(title);
                p.setContent("【晨会" + typeName + "】" + speaker + "：" + content);
                p.setSourceType(1);
                p.setStatus(0);
                problemMapper.insert(p);
                imported++;
                messages.add(title);
            }
        }

        // 2. 会议摘要：作为一个跟进事项
        JsonNode summaryData = getData("/api/meeting/summary/export/" + meetingId);
        if (summaryData != null) {
            String summary = summaryData.path("content").asText("");
            if (!summary.isBlank()) {
                String title = "晨会摘要跟进事项（会议" + meetingId + "）";
                if (!exists(meetingId, title)) {
                    Problem p = new Problem();
                    p.setMeetingId(meetingId);
                    p.setTitle(title);
                    p.setContent(summary);
                    p.setSourceType(1);
                    p.setStatus(0);
                    problemMapper.insert(p);
                    imported++;
                } else {
                    skipped++;
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("meetingId", meetingId);
        result.put("imported", imported);
        result.put("skipped", skipped);
        result.put("messages", messages);
        return result;
    }

    private boolean exists(Long meetingId, String title) {
        Long count = problemMapper.selectCount(new LambdaQueryWrapper<Problem>()
                .eq(Problem::getMeetingId, meetingId)
                .eq(Problem::getTitle, title));
        return count != null && count > 0;
    }

    private JsonNode getData(String path) {
        try {
            ResponseEntity<String> resp = restTemplate.getForEntity(reportBaseUrl + path, String.class);
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                return null;
            }
            JsonNode root = objectMapper.readTree(resp.getBody());
            if (!root.path("success").asBoolean(false)) {
                return null;
            }
            return root.path("data");
        } catch (Exception e) {
            return null;
        }
    }
}
