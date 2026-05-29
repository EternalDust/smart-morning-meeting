package com.huadi.smm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huadi.smm.common.Result;
import com.huadi.smm.dao.MemberMapper;
import com.huadi.smm.entity.Member;
import com.huadi.smm.entity.SpeechRecord;
import com.huadi.smm.entity.MeetingSummary;
import com.huadi.smm.service.ReportService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/meeting")
public class ReportController {

    @Resource
    private ReportService reportService;

    @Resource
    private MemberMapper memberMapper;

    @PostMapping("/speech/save")
    public Result<?> saveSpeech(@RequestBody SpeechRecord record) {
        try {
            return Result.ok(reportService.saveSpeech(record));
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PutMapping("/speech/{id}")
    public Result<SpeechRecord> updateSpeech(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return Result.ok(reportService.updateSpeech(id, body.get("content"), body.get("keyPoints")));
    }

    @GetMapping("/speech/list/{meetingId}")
    public Result<Map<String, Object>> listSpeech(@PathVariable Long meetingId) {
        List<SpeechRecord> records = reportService.listByMeetingId(meetingId);
        List<String> speakerIds = records.stream().map(SpeechRecord::getSpeakerId).distinct().collect(Collectors.toList());
        Map<String, String> nameMap = buildNameMap(speakerIds);

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("nameMap", nameMap);
        return Result.ok(result);
    }

    @PostMapping("/summary/save")
    public Result<MeetingSummary> saveSummary(@RequestBody Map<String, Object> body) {
        Long meetingId = Long.valueOf(body.get("meetingId").toString());
        String summary = (String) body.get("summary");
        return Result.ok(reportService.saveSummary(meetingId, summary));
    }

    @GetMapping("/summary/{meetingId}")
    public Result<MeetingSummary> getSummary(@PathVariable Long meetingId) {
        return Result.ok(reportService.getSummary(meetingId));
    }

    @GetMapping("/summary/export/{meetingId}")
    public Result<Map<String, String>> exportSummary(@PathVariable Long meetingId) {
        MeetingSummary summary = reportService.getSummary(meetingId);
        Map<String, String> result = new HashMap<>();
        result.put("content", summary != null ? summary.getSummary() : "");
        result.put("meetingId", String.valueOf(meetingId));
        return Result.ok(result);
    }

    private Map<String, String> buildNameMap(List<String> userIds) {
        Map<String, String> nameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            LambdaQueryWrapper<Member> qw = new LambdaQueryWrapper<>();
            qw.in(Member::getUserId, userIds);
            for (Member m : memberMapper.selectList(qw)) {
                nameMap.put(m.getUserId(), m.getName());
            }
        }
        return nameMap;
    }
}
