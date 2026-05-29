package com.huadi.smm.service;

import com.huadi.smm.dao.MeetingAttendeeMapper;
import com.huadi.smm.entity.MeetingAttendee;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class AttendeeService {

    @Autowired
    private MeetingAttendeeMapper meetingAttendeeMapper;

    public List<MeetingAttendee> listByMeetingId(Long meetingId) {
        QueryWrapper<MeetingAttendee> wrapper = new QueryWrapper<>();
        wrapper.eq("meeting_id", meetingId);
        return meetingAttendeeMapper.selectList(wrapper);
    }

    public MeetingAttendee addAttendee(MeetingAttendee attendee) {
        attendee.setInviteTime(new Date());
        meetingAttendeeMapper.insert(attendee);
        return attendee;
    }

    public void deleteAttendee(Long id) {
        meetingAttendeeMapper.deleteById(id);
    }
}