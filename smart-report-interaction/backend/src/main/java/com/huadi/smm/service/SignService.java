package com.huadi.smm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huadi.smm.dao.AttendeeMapper;
import com.huadi.smm.dao.MeetingInfoMapper;
import com.huadi.smm.dao.SignInMapper;
import com.huadi.smm.entity.Attendee;
import com.huadi.smm.entity.MeetingInfo;
import com.huadi.smm.entity.SignIn;
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

    public Result signIn(Long meetingId, Long userId, Integer signType) {
        String userIdStr = String.valueOf(userId);
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
        sq.eq(SignIn::getMeetingId, meetingId).eq(SignIn::getUserId, userIdStr);
        SignIn existing = signInMapper.selectOne(sq);
        if (existing != null) {
            return new Result("已签到，请勿重复签到");
        }

        SignIn record = new SignIn();
        record.setMeetingId(meetingId);
        record.setUserId(userIdStr);
        record.setSignType(signType);
        record.setSignTime(LocalDateTime.now().format(FMT));

        int status = 0;
        String startTime = meeting.getStartTime();
        if (startTime != null && !startTime.trim().isEmpty()) {
            try {
                LocalDateTime start = LocalDateTime.parse(startTime.trim(), FMT);
                if (LocalDateTime.now().isAfter(start)) {
                    status = 1;
                }
            } catch (Exception ignored) {
                // 会议开始时间解析失败时按准时处理，避免误判迟到
            }
        }
        record.setSignStatus(status);

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
