package com.huadi.smm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huadi.smm.ai.AiClient;
import com.huadi.smm.ai.dto.SpeechLine;
import com.huadi.smm.ai.dto.SummaryResult;
import com.huadi.smm.dao.MeetingInfoMapper;
import com.huadi.smm.dao.SpeechRecordMapper;
import com.huadi.smm.dao.MeetingSummaryMapper;
import com.huadi.smm.entity.MeetingInfo;
import com.huadi.smm.entity.SpeechRecord;
import com.huadi.smm.entity.MeetingSummary;
import com.huadi.smm.ws.RealtimeServer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    @Resource
    private SpeechRecordMapper speechMapper;

    @Resource
    private MeetingSummaryMapper summaryMapper;

    @Resource
    private MeetingInfoMapper meetingInfoMapper;

    @Resource
    private AiClient aiClient;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SpeechRecord saveSpeech(SpeechRecord record) {
        MeetingInfo meeting = meetingInfoMapper.selectById(record.getMeetingId());
        if (meeting == null) {
            throw new RuntimeException("晨会不存在");
        }
        record.setSpeechTime(LocalDateTime.now().format(FMT));
        speechMapper.insert(record);
        return record;
    }

    public SpeechRecord updateSpeech(Long id, String content, String keyPoints) {
        SpeechRecord record = speechMapper.selectById(id);
        if (record != null) {
            record.setContent(content);
            record.setKeyPoints(keyPoints);
            speechMapper.updateById(record);
        }
        return record;
    }

    public List<SpeechRecord> listByMeetingId(Long meetingId) {
        LambdaQueryWrapper<SpeechRecord> qw = new LambdaQueryWrapper<>();
        qw.eq(SpeechRecord::getMeetingId, meetingId).orderByAsc(SpeechRecord::getSpeechTime);
        return speechMapper.selectList(qw);
    }

    public MeetingSummary saveSummary(Long meetingId, String summaryText) {
        LambdaQueryWrapper<MeetingSummary> qw = new LambdaQueryWrapper<>();
        qw.eq(MeetingSummary::getMeetingId, meetingId);
        MeetingSummary summary = summaryMapper.selectOne(qw);
        if (summary == null) {
            summary = new MeetingSummary();
            summary.setMeetingId(meetingId);
        }
        summary.setSummary(summaryText);
        summary.setCreateTime(LocalDateTime.now().format(FMT));
        if (summary.getId() == null) {
            summaryMapper.insert(summary);
        } else {
            summaryMapper.updateById(summary);
        }
        return summary;
    }

    public MeetingSummary getSummary(Long meetingId) {
        LambdaQueryWrapper<MeetingSummary> qw = new LambdaQueryWrapper<>();
        qw.eq(MeetingSummary::getMeetingId, meetingId);
        return summaryMapper.selectOne(qw);
    }

    public SummaryResult generateSummary(Long meetingId) {
        MeetingInfo meeting = meetingInfoMapper.selectById(meetingId);
        String title = meeting != null && meeting.getTitle() != null ? meeting.getTitle() : "周一科室晨会";
        List<SpeechLine> lines = new ArrayList<>();
        for (SpeechRecord r : listByMeetingId(meetingId)) {
            lines.add(new SpeechLine(r.getSpeakerId(), r.getContent()));
        }
        if (lines.isEmpty()) {
            SummaryResult empty = new SummaryResult();
            empty.setSummary("当前会议暂无发言记录");
            return empty;
        }
        SummaryResult result = aiClient.generateSummary(title, lines);
        saveSummary(meetingId, toJson(result));
        broadcast(meetingId, "summary");
        return result;
    }

    public SummaryResult getSummaryResult(Long meetingId) {
        MeetingSummary summary = getSummary(meetingId);
        if (summary == null || summary.getSummary() == null || summary.getSummary().isEmpty()) {
            SummaryResult empty = new SummaryResult();
            empty.setSummary("会议进行中...");
            return empty;
        }
        String stored = summary.getSummary();
        try {
            return MAPPER.readValue(stored, SummaryResult.class);
        } catch (Exception e) {
            SummaryResult fallback = new SummaryResult();
            fallback.setSummary(stored);
            return fallback;
        }
    }

    public String getSummaryText(Long meetingId) {
        return getSummaryResult(meetingId).getSummary();
    }

    private String toJson(SummaryResult result) {
        try {
            return MAPPER.writeValueAsString(result);
        } catch (Exception e) {
            return result.getSummary();
        }
    }

    private void broadcast(Long meetingId, String type) {
        try {
            Map<String, Object> wsMsg = new HashMap<>();
            wsMsg.put("type", type);
            RealtimeServer.broadcast(meetingId, MAPPER.writeValueAsString(wsMsg));
        } catch (Exception ignored) {}
    }
}
