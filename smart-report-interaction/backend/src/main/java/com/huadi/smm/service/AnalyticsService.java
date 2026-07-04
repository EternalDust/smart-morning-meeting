package com.huadi.smm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huadi.smm.dao.DepartmentAnalyticsMapper;
import com.huadi.smm.dao.MeetingAnalyticsMapper;
import com.huadi.smm.dao.MemberAnalyticsMapper;
import com.huadi.smm.entity.DepartmentAnalytics;
import com.huadi.smm.entity.MeetingAnalytics;
import com.huadi.smm.entity.MemberAnalytics;
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
}
