package com.huadi.smm.service;

import com.huadi.smm.entity.DataCleanData;
import com.huadi.smm.entity.SmGmMember;
import com.huadi.smm.entity.SmMeetingAttendee;
import com.huadi.smm.entity.SmMeetingInfo;
import com.huadi.smm.entity.SmMeetingInteraction;
import com.huadi.smm.entity.SmMeetingSignin;
import com.huadi.smm.entity.SmMeetingSpeech;
import com.huadi.smm.entity.SmProblem;
import com.huadi.smm.mapper.DataCleanDataMapper;
import com.huadi.smm.mapper.SmGmMemberMapper;
import com.huadi.smm.mapper.SmMeetingAttendeeMapper;
import com.huadi.smm.mapper.SmMeetingInfoMapper;
import com.huadi.smm.mapper.SmMeetingInteractionMapper;
import com.huadi.smm.mapper.SmMeetingSigninMapper;
import com.huadi.smm.mapper.SmMeetingSpeechMapper;
import com.huadi.smm.mapper.SmProblemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * 大屏真实数据聚合服务。
 *
 * 直接读取共享库 sm_meeting_* / sm_problem / data_clean_data 做实时汇总，
 * 替代原先仅读 mock 表 bi_stat_* 的写法。
 *
 * 数据范围：role=1 管理层看全院；role=2 科室人员仅统计本科室成员产生的记录；
 * 无 token 演示模式按管理层处理（全院）。
 * 日期取“实际存在数据”的最近 N 天（生成数据集中在 2026 年，非当天日历）。
 */
@Service
@RequiredArgsConstructor
public class DashboardAggregateService {

    /** 大屏趋势展示的天数 */
    public static final int TREND_DAYS = 7;

    private final SmMeetingInfoMapper meetingInfoMapper;
    private final SmMeetingAttendeeMapper attendeeMapper;
    private final SmMeetingSigninMapper signinMapper;
    private final SmMeetingSpeechMapper speechMapper;
    private final SmMeetingInteractionMapper interactionMapper;
    private final SmProblemMapper problemMapper;
    private final DataCleanDataMapper dataCleanDataMapper;
    private final SmGmMemberMapper gmMemberMapper;

    /** 工号 -> 科室 */
    private Map<String, String> buildUserDeptMap() {
        Map<String, String> map = new HashMap<>();
        for (SmGmMember m : gmMemberMapper.selectList(null)) {
            if (m.getUserId() != null && m.getDept() != null) {
                map.put(m.getUserId(), m.getDept());
            }
        }
        return map;
    }

    /** 人员表主键 id -> 科室（sm_problem.creator_id 存的是成员 id） */
    private Map<Long, String> buildMemberIdDeptMap() {
        Map<Long, String> map = new HashMap<>();
        for (SmGmMember m : gmMemberMapper.selectList(null)) {
            if (m.getId() != null && m.getDept() != null) {
                map.put(m.getId(), m.getDept());
            }
        }
        return map;
    }

    /** 会议 id -> 会议日期(yyyy-MM-dd)，仅保留有开始时间的会议 */
    private Map<Long, String> buildMeetingDateMap() {
        Map<Long, String> map = new HashMap<>();
        for (SmMeetingInfo m : meetingInfoMapper.selectList(null)) {
            if (m.getStartTime() != null) {
                map.put(m.getId(), m.getStartTime().toLocalDate().toString());
            }
        }
        return map;
    }

    /** 数据范围过滤：管理层/演示模式不过滤；科室人员仅统计本科室成员 */
    private boolean inDept(String userId, String dept, Map<String, String> userDept) {
        if (dept == null) {
            return true;
        }
        return dept.equals(userDept.get(userId));
    }

    /**
     * 近 N 天晨会核心统计（按日期升序）。
     * 返回 date -> [实到, 应到, 发言数, 互动数]
     */
    private Map<String, int[]> computeMeetingStats(String dept) {
        Map<Long, String> meetingDate = buildMeetingDateMap();
        Map<String, String> userDept = buildUserDeptMap();

        Set<Long> scope = new HashSet<>();
        Map<Long, Integer> shouldNum = new HashMap<>();
        for (SmMeetingAttendee a : attendeeMapper.selectList(null)) {
            String userId = a.getUserId() == null ? null : String.valueOf(a.getUserId());
            if (meetingDate.containsKey(a.getMeetingId()) && inDept(userId, dept, userDept)) {
                scope.add(a.getMeetingId());
                shouldNum.merge(a.getMeetingId(), 1, Integer::sum);
            }
        }

        Map<Long, Set<String>> realUsers = new HashMap<>();
        for (SmMeetingSignin s : signinMapper.selectList(null)) {
            if (scope.contains(s.getMeetingId()) && inDept(s.getUserId(), dept, userDept)) {
                realUsers.computeIfAbsent(s.getMeetingId(), k -> new HashSet<>()).add(s.getUserId());
            }
        }

        Map<Long, Integer> speechNum = new HashMap<>();
        for (SmMeetingSpeech s : speechMapper.selectList(null)) {
            if (scope.contains(s.getMeetingId()) && inDept(s.getSpeakerId(), dept, userDept)) {
                speechNum.merge(s.getMeetingId(), 1, Integer::sum);
            }
        }

        Map<Long, Integer> interactNum = new HashMap<>();
        for (SmMeetingInteraction i : interactionMapper.selectList(null)) {
            if (scope.contains(i.getMeetingId()) && inDept(i.getUserId(), dept, userDept)) {
                interactNum.merge(i.getMeetingId(), 1, Integer::sum);
            }
        }

        Map<String, int[]> stat = new TreeMap<>();
        for (Long mid : scope) {
            String date = meetingDate.get(mid);
            int[] arr = stat.computeIfAbsent(date, k -> new int[4]);
            arr[0] += realUsers.getOrDefault(mid, Collections.emptySet()).size();
            arr[1] += shouldNum.getOrDefault(mid, 0);
            arr[2] += speechNum.getOrDefault(mid, 0);
            arr[3] += interactNum.getOrDefault(mid, 0);
        }
        return stat;
    }

    /** 取最近 N 天（升序）；日期不足 N 天时返回全部 */
    private List<String> lastNDates(Set<String> dates, int n) {
        List<String> list = new ArrayList<>(dates);
        Collections.sort(list);
        return list.subList(Math.max(0, list.size() - n), list.size());
    }

    private BigDecimal rate(int real, int should) {
        if (should <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(real * 100.0 / should).setScale(1, RoundingMode.HALF_UP);
    }

    /** 按日期统计 data_clean_data 平均质量分（按科室过滤） */
    private Map<String, BigDecimal> buildQualityByDate(String dept) {
        Map<String, BigDecimal> sum = new HashMap<>();
        Map<String, Integer> cnt = new HashMap<>();
        for (DataCleanData d : dataCleanDataMapper.selectList(null)) {
            if (d.getVisitTime() == null || d.getQualityScore() == null) {
                continue;
            }
            if (dept != null && !dept.equals(d.getDepartment())) {
                continue;
            }
            String date = d.getVisitTime().toLocalDate().toString();
            sum.merge(date, d.getQualityScore(), BigDecimal::add);
            cnt.merge(date, 1, Integer::sum);
        }
        Map<String, BigDecimal> avg = new HashMap<>();
        for (Map.Entry<String, BigDecimal> e : sum.entrySet()) {
            avg.put(e.getKey(), e.getValue().divide(BigDecimal.valueOf(cnt.get(e.getKey())), 1, RoundingMode.HALF_UP));
        }
        return avg;
    }

    /**
     * 近 7 日参会率趋势，返回 {dates, rates}（前端图表契约）。
     * 无真实数据时回退到演示周数据，保证大屏不为空。
     */
    public Map<String, Object> getTrend(String dept) {
        Map<String, int[]> stat = computeMeetingStats(dept);
        List<String> dates = lastNDates(stat.keySet(), TREND_DAYS);

        List<String> dateList = new ArrayList<>();
        List<BigDecimal> rateList = new ArrayList<>();
        for (String date : dates) {
            int[] arr = stat.get(date);
            dateList.add(date);
            rateList.add(rate(arr[0], arr[1]));
        }

        if (dateList.isEmpty()) {
            dateList.addAll(List.of("周一", "周二", "周三", "周四", "周五", "周六", "周日"));
            rateList.addAll(List.of(
                new BigDecimal("92"), new BigDecimal("95"), new BigDecimal("88"),
                new BigDecimal("98"), new BigDecimal("96"), new BigDecimal("99"),
                new BigDecimal("93")));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dates", dateList);
        result.put("rates", rateList);
        return result;
    }

    /**
     * 会议数据概览（大屏新面板）：按日期展示应到/实到/参会率/发言/互动/医疗质量分。
     */
    public List<Map<String, Object>> getMeetingOverview(String dept) {
        Map<String, int[]> stat = computeMeetingStats(dept);
        List<String> dates = lastNDates(stat.keySet(), TREND_DAYS);
        Map<String, BigDecimal> qualityByDate = buildQualityByDate(dept);

        List<Map<String, Object>> result = new ArrayList<>();
        for (String date : dates) {
            int[] arr = stat.get(date);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", date);
            row.put("shouldNum", arr[1]);
            row.put("realNum", arr[0]);
            row.put("attendRate", rate(arr[0], arr[1]));
            row.put("speechCount", arr[2]);
            row.put("interactionCount", arr[3]);
            row.put("qualityScore", qualityByDate.getOrDefault(date, BigDecimal.ZERO));
            result.add(row);
        }
        return result;
    }

    /**
     * 问题部门分布：按 sm_problem 归属人（creator/assignee）的科室分组。
     * 无问题时回退到演示分布数据，保证饼图不为空。
     */
    public List<Map<String, Object>> getIssuesDistribution(String dept) {
        List<SmProblem> problems = problemMapper.selectList(null);
        Map<Long, String> memberIdDept = buildMemberIdDeptMap();

        Map<String, Integer> countByDept = new LinkedHashMap<>();
        for (SmProblem p : problems) {
            Long owner = p.getCreatorId() != null ? p.getCreatorId() : p.getAssigneeId();
            String d = owner == null ? null : memberIdDept.get(owner);
            if (d == null || "管理层".equals(d)) {
                d = "其他";
            }
            countByDept.merge(d, 1, Integer::sum);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        if (dept != null) {
            result.add(Map.of("name", dept, "value", countByDept.getOrDefault(dept, 0)));
        } else {
            for (Map.Entry<String, Integer> e : countByDept.entrySet()) {
                result.add(Map.of("name", e.getKey(), "value", e.getValue()));
            }
        }

        if (result.isEmpty()) {
            result.add(Map.of("name", "心内科", "value", 1048));
            result.add(Map.of("name", "神经内科", "value", 735));
            result.add(Map.of("name", "急诊科", "value", 580));
            result.add(Map.of("name", "外科", "value", 484));
        }
        return result;
    }
}
