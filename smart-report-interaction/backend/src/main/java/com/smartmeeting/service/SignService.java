package com.smartmeeting.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartmeeting.dao.AttendeeMapper;
import com.smartmeeting.dao.MeetingInfoMapper;
import com.smartmeeting.dao.SignInMapper;
import com.smartmeeting.entity.Attendee;
import com.smartmeeting.entity.MeetingInfo;
import com.smartmeeting.entity.SignIn;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SignService {

    @Resource
    private SignInMapper signInMapper;

    @Resource
    private AttendeeMapper attendeeMapper;

    @Resource
    private MeetingInfoMapper meetingInfoMapper;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Result signIn(Long meetingId, String userId, Integer signType) {
        MeetingInfo meeting = meetingInfoMapper.selectById(meetingId);
        if (meeting == null) {
            return new Result("晨会不存在");
        }

        LambdaQueryWrapper<Attendee> aq = new LambdaQueryWrapper<>();
        aq.eq(Attendee::getMeetingId, meetingId).eq(Attendee::getUserId, userId);
        Attendee attendee = attendeeMapper.selectOne(aq);
        if (attendee == null) {
            return new Result("您不在本次晨会参会名单中");
        }

        LambdaQueryWrapper<SignIn> sq = new LambdaQueryWrapper<>();
        sq.eq(SignIn::getMeetingId, meetingId).eq(SignIn::getUserId, userId);
        SignIn existing = signInMapper.selectOne(sq);
        if (existing != null) {
            return new Result("已签到，请勿重复签到");
        }

        SignIn record = new SignIn();
        record.setMeetingId(meetingId);
        record.setUserId(userId);
        record.setSignType(signType);
        record.setSignTime(LocalDateTime.now().format(FMT));

        int hour = LocalDateTime.now().getHour();
        int minute = LocalDateTime.now().getMinute();
        record.setSignStatus((hour > 8 || (hour == 8 && minute > 30)) ? 1 : 0);

        signInMapper.insert(record);
        attendee.setAttendStatus(1);
        attendeeMapper.updateById(attendee);

        return new Result(record);
    }

    public List<SignIn> listByMeetingId(Long meetingId) {
        LambdaQueryWrapper<SignIn> qw = new LambdaQueryWrapper<>();
        qw.eq(SignIn::getMeetingId, meetingId).orderByAsc(SignIn::getSignTime);
        return signInMapper.selectList(qw);
    }

    public int countAttendees(Long meetingId) {
        LambdaQueryWrapper<Attendee> qw = new LambdaQueryWrapper<>();
        qw.eq(Attendee::getMeetingId, meetingId);
        return attendeeMapper.selectCount(qw).intValue();
    }

    public int countSignedIn(Long meetingId) {
        LambdaQueryWrapper<SignIn> qw = new LambdaQueryWrapper<>();
        qw.eq(SignIn::getMeetingId, meetingId);
        return signInMapper.selectCount(qw).intValue();
    }

    public int countByStatus(Long meetingId, Integer status) {
        LambdaQueryWrapper<SignIn> qw = new LambdaQueryWrapper<>();
        qw.eq(SignIn::getMeetingId, meetingId).eq(SignIn::getSignStatus, status);
        return signInMapper.selectCount(qw).intValue();
    }

    public static class Result {
        public final boolean success;
        public final SignIn data;
        public final String error;

        Result(SignIn data) { this.success = true; this.data = data; this.error = null; }
        Result(String error) { this.success = false; this.data = null; this.error = error; }
    }
}
