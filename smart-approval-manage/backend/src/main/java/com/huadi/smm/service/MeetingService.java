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

    public List<MeetingInfo> listAll() {
        return meetingInfoMapper.selectList(null);
    }

    public MeetingInfo getById(Long id) {
        return meetingInfoMapper.selectById(id);
    }

    public MeetingInfo save(MeetingInfo meeting) {
        if (meeting.getApproveStatus() == null) {
            meeting.setApproveStatus(0);
        }
        meeting.setCreateTime(new Date());
        meeting.setUpdateTime(new Date());
        meetingInfoMapper.insert(meeting);
        eventProducer.send(MeetingEvent.of("MEETING_CREATED", meeting.getId(), null, meeting.getApproveStatus()));
        return meeting;
    }

    public boolean archiveMeeting(Long meetingId) {
        MeetingInfo meeting = meetingInfoMapper.selectById(meetingId);
        if (meeting == null || meeting.getApproveStatus() == null || meeting.getApproveStatus() != 2) {
            return false;
        }
        meeting.setApproveStatus(4);
        meeting.setUpdateTime(new Date());
        meetingInfoMapper.updateById(meeting);
        eventProducer.send(MeetingEvent.of("APPROVE_STATUS_CHANGED", meetingId, 2, 4));
        return true;
    }
}