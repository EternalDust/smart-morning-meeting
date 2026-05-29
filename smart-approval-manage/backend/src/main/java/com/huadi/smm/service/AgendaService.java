package com.huadi.smm.service;

import com.huadi.smm.dao.MeetingAgendaMapper;
import com.huadi.smm.dao.MeetingInfoMapper;
import com.huadi.smm.entity.MeetingAgenda;
import com.huadi.smm.entity.MeetingInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class AgendaService {

    @Autowired
    private MeetingInfoMapper meetingInfoMapper;

    @Autowired
    private MeetingAgendaMapper meetingAgendaMapper;

    @Transactional
    public List<String> createAiAgenda(Long meetingId, String deptName, Integer meetingType) {
        MeetingInfo meeting = meetingInfoMapper.selectById(meetingId);
        if (meeting == null) {
            throw new RuntimeException("会议ID " + meetingId + " 不存在");
        }
        List<String> agendaList = generateAgendaByRule(deptName, meetingType);
        for (int i = 0; i < agendaList.size(); i++) {
            MeetingAgenda agenda = new MeetingAgenda();
            agenda.setMeetingId(meetingId);
            agenda.setTitle(agendaList.get(i));
            agenda.setSort(i + 1);
            agenda.setCreateTime(new Date());
            meetingAgendaMapper.insert(agenda);
        }
        return agendaList;
    }

    private List<String> generateAgendaByRule(String deptName, Integer meetingType) {
        List<String> lib = new ArrayList<>(Arrays.asList(
                "科室工作进度汇报", "医疗质量问题复盘", "患者投诉问题分析",
                "排班与人力协调", "院内新规传达学习", "重点病例讨论会诊"
        ));
        Collections.shuffle(lib);
        int count = Math.min(lib.size(), 3 + (int)(Math.random() * 3));
        return lib.subList(0, count);
    }
}