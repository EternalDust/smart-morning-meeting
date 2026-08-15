package com.huadi.smm.service;

import com.huadi.smm.dao.MeetingInfoMapper;
import com.huadi.smm.entity.MeetingInfo;
import com.huadi.smm.event.MeetingEvent;
import com.huadi.smm.producer.MeetingEventProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class MeetingService {

    @Autowired
    private MeetingInfoMapper meetingInfoMapper;

    @Autowired
    private MeetingEventProducer eventProducer;

    @Autowired
    private MeetingCacheService meetingCacheService;

    @Autowired
    private AuditLogService auditLogService;

    public List<MeetingInfo> listAll() {
        return meetingInfoMapper.selectList(null);
    }

    public List<MeetingInfo> list(com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<MeetingInfo> qw) {
        return meetingInfoMapper.selectList(qw);
    }

    public MeetingInfo getById(Long id) {
        MeetingInfo cached = meetingCacheService.getCachedMeeting(id);
        if (cached != null) return cached;
        MeetingInfo db = meetingInfoMapper.selectById(id);
        if (db != null) meetingCacheService.cacheMeeting(db);
        return db;
    }

    public MeetingInfo save(MeetingInfo meeting) {
        if (meeting.getApproveStatus() == null) {
            meeting.setApproveStatus(0);
        }
        meeting.setCreateTime(new Date());
        meeting.setUpdateTime(new Date());
        meetingInfoMapper.insert(meeting);
        meetingCacheService.cacheMeeting(meeting);
        eventProducer.send(MeetingEvent.of("MEETING_CREATED", meeting.getId(), null, meeting.getApproveStatus()));
        auditLogService.log("CREATE_MEETING", meeting.getId(), "meeting", meeting.getCreatorId(), "创建会议: " +
                meeting.getTitle());
        return meeting;
    }

    public boolean publishMeeting(Long meetingId) {
        MeetingInfo meeting = meetingInfoMapper.selectById(meetingId);
        if (meeting == null || meeting.getApproveStatus() == null || meeting.getApproveStatus() != 2) {
            return false;
        }
        meeting.setApproveStatus(5);
        meeting.setUpdateTime(new Date());
        meetingInfoMapper.updateById(meeting);
        meetingCacheService.deleteMeetingCache(meetingId);
        auditLogService.log("PUBLISH_MEETING", meetingId, "meeting", null, "发布会议");
        return true;
    }

    public boolean archiveMeeting(Long meetingId) {
        MeetingInfo meeting = meetingInfoMapper.selectById(meetingId);
        if (meeting == null || meeting.getApproveStatus() == null || meeting.getApproveStatus() != 5) {
            return false;
        }
        meeting.setApproveStatus(4);
        meeting.setUpdateTime(new Date());
        meetingInfoMapper.updateById(meeting);
        meetingCacheService.deleteMeetingCache(meetingId);
        auditLogService.log("ARCHIVE_MEETING", meetingId, "meeting", null, "归档会议");
        return true;
    }
}