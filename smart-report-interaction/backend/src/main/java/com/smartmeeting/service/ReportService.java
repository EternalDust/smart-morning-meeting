package com.smartmeeting.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartmeeting.dao.MeetingInfoMapper;
import com.smartmeeting.dao.SpeechRecordMapper;
import com.smartmeeting.dao.MeetingSummaryMapper;
import com.smartmeeting.entity.MeetingInfo;
import com.smartmeeting.entity.SpeechRecord;
import com.smartmeeting.entity.MeetingSummary;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {

    @Resource
    private SpeechRecordMapper speechMapper;

    @Resource
    private MeetingSummaryMapper summaryMapper;

    @Resource
    private MeetingInfoMapper meetingInfoMapper;

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
}
