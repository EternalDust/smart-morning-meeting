package com.huadi.smm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huadi.smm.dao.DepartmentAnalyticsMapper;
import com.huadi.smm.dao.MeetingAnalyticsMapper;
import com.huadi.smm.dao.MemberAnalyticsMapper;
import com.huadi.smm.dao.WeeklyTrendMapper;
import com.huadi.smm.dao.TimeAnalyticsMapper;
import com.huadi.smm.entity.DepartmentAnalytics;
import com.huadi.smm.entity.MeetingAnalytics;
import com.huadi.smm.entity.MemberAnalytics;
import com.huadi.smm.entity.WeeklyTrend;
import com.huadi.smm.entity.TimeAnalytics;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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

    public MeetingAnalytics getByMeetingId(Long meetingId) {
        LambdaQueryWrapper<MeetingAnalytics> qw = new LambdaQueryWrapper<>();
        qw.eq(MeetingAnalytics::getMeetingId, meetingId);
        return meetingAnalyticsMapper.selectOne(qw);
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
