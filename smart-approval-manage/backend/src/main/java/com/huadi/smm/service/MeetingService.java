package com.huadi.smm.service;

import com.huadi.smm.dao.MeetingInfoMapper;
import com.huadi.smm.entity.MeetingInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class MeetingService {

    @Autowired
    private MeetingInfoMapper meetingInfoMapper;

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
        return true;
    }
}