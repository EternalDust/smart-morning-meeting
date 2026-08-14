package com.huadi.smm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huadi.smm.dao.AttendeeMapper;
import com.huadi.smm.dao.DepartmentAnalyticsMapper;
import com.huadi.smm.dao.InteractionMapper;
import com.huadi.smm.dao.MeetingAnalyticsMapper;
import com.huadi.smm.dao.MemberAnalyticsMapper;
import com.huadi.smm.dao.SignInMapper;
import com.huadi.smm.dao.SpeechRecordMapper;
import com.huadi.smm.dao.WeeklyTrendMapper;
import com.huadi.smm.dao.TimeAnalyticsMapper;
import com.huadi.smm.entity.Attendee;
import com.huadi.smm.entity.DepartmentAnalytics;
import com.huadi.smm.entity.Interaction;
import com.huadi.smm.entity.MeetingAnalytics;
import com.huadi.smm.entity.MemberAnalytics;
import com.huadi.smm.entity.SignIn;
import com.huadi.smm.entity.SpeechRecord;
import com.huadi.smm.entity.WeeklyTrend;
import com.huadi.smm.entity.TimeAnalytics;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class AnalyticsService {

    @Resource
    private MeetingAnalyticsMapper meetingAnalyticsMapper;

    @Resource
    private DepartmentAnalyticsMapper departmentAnalyticsMapper;

    @Resource
    private MemberAnalyticsMapper memberAnalyticsMapper;

    @Resource
    private WeeklyTrendMapper weeklyTrendMapper;

    @Resource
    private TimeAnalyticsMapper timeAnalyticsMapper;

    @Resource
    private SignInMapper signInMapper;

    @Resource
    private AttendeeMapper attendeeMapper;

    @Resource
    private SpeechRecordMapper speechRecordMapper;

    @Resource
    private InteractionMapper interactionMapper;

    public MeetingAnalytics getByMeetingId(Long meetingId) {
        LambdaQueryWrapper<MeetingAnalytics> qw = new LambdaQueryWrapper<>();
        qw.eq(MeetingAnalytics::getMeetingId, meetingId);
        MeetingAnalytics a = meetingAnalyticsMapper.selectOne(qw);
        if (a == null) {
            return null;
        }

        int shouldAttend = attendeeMapper.selectCount(
                new LambdaQueryWrapper<Attendee>().eq(Attendee::getMeetingId, meetingId)).intValue();
        int signed = signInMapper.selectCount(
                new LambdaQueryWrapper<SignIn>().eq(SignIn::getMeetingId, meetingId)).intValue();
        int normal = signInMapper.selectCount(
                new LambdaQueryWrapper<SignIn>().eq(SignIn::getMeetingId, meetingId).eq(SignIn::getSignStatus, 0)).intValue();
        int late = signInMapper.selectCount(
                new LambdaQueryWrapper<SignIn>().eq(SignIn::getMeetingId, meetingId).eq(SignIn::getSignStatus, 1)).intValue();
        int speech = speechRecordMapper.selectCount(
                new LambdaQueryWrapper<SpeechRecord>().eq(SpeechRecord::getMeetingId, meetingId)).intValue();
        int interaction = interactionMapper.selectCount(
                new LambdaQueryWrapper<Interaction>().eq(Interaction::getMeetingId, meetingId)
                        .in(Interaction::getInteractType, 1, 2, 3)).intValue();

        double attendRate = shouldAttend == 0 ? 0 : signed * 100.0 / shouldAttend;
        double score = attendRate * 0.4 + Math.min(speech / 5.0, 1.0) * 30 + Math.min(interaction / 8.0, 1.0) * 30;

        a.setShouldAttend(shouldAttend);
        a.setActualAttend(signed);
        a.setAttendRate(BigDecimal.valueOf(attendRate).setScale(2, RoundingMode.HALF_UP));
        a.setNormalCount(normal);
        a.setLateCount(late);
        a.setSpeechCount(speech);
        a.setInteractionCount(interaction);
        a.setQualityScore(BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP));
        return a;
    }

    public List<MeetingAnalytics> getMeetingTrend() {
        LambdaQueryWrapper<MeetingAnalytics> qw = new LambdaQueryWrapper<>();
        qw.orderByAsc(MeetingAnalytics::getMeetingDate);
        return meetingAnalyticsMapper.selectList(qw);
    }

    public List<DepartmentAnalytics> getDepartmentRanking() {
        LambdaQueryWrapper<DepartmentAnalytics> qw = new LambdaQueryWrapper<>();
        qw.orderByDesc(DepartmentAnalytics::getAvgAttendRate);
        return departmentAnalyticsMapper.selectList(qw);
    }

    public MemberAnalytics getByUserId(String userId) {
        LambdaQueryWrapper<MemberAnalytics> qw = new LambdaQueryWrapper<>();
        qw.eq(MemberAnalytics::getUserId, userId);
        return memberAnalyticsMapper.selectOne(qw);
    }

    public List<WeeklyTrend> getWeeklyTrend() {
        LambdaQueryWrapper<WeeklyTrend> qw = new LambdaQueryWrapper<>();
        qw.orderByAsc(WeeklyTrend::getMeetingWeek);
        return weeklyTrendMapper.selectList(qw);
    }

    public List<TimeAnalytics> getTimePattern() {
        return timeAnalyticsMapper.selectList(null);
    }
}
