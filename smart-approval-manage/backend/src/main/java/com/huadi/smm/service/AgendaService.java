package com.huadi.smm.service;

import com.huadi.smm.dao.MeetingAgendaRepository;
import com.huadi.smm.dao.MeetingInfoRepository;
import com.huadi.smm.entity.MeetingAgenda;
import com.huadi.smm.entity.MeetingInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class AgendaService {

    @Autowired
    private MeetingInfoRepository meetingInfoRepository;

    @Autowired
    private MeetingAgendaRepository meetingAgendaRepository;

    @Transactional
    public List<String> createAiAgenda(Long meetingId, String deptName, Integer meetingType) {
        MeetingInfo meeting = meetingInfoRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("会议ID " + meetingId + " 不存在"));

        List<String> agendaList = generateAgendaByRule(deptName, meetingType);

        List<MeetingAgenda> entities = new ArrayList<>();
        for (int i = 0; i < agendaList.size(); i++) {
            MeetingAgenda agenda = new MeetingAgenda();
            agenda.setMeetingId(meetingId);
            agenda.setTitle(agendaList.get(i));
            agenda.setSort(i + 1);
            agenda.setCreateTime(new Date());
            entities.add(agenda);
        }
        meetingAgendaRepository.saveAll(entities);
        return agendaList;
    }

    private List<String> generateAgendaByRule(String deptName, Integer meetingType) {
        List<String> lib = new java.util.ArrayList<>(java.util.Arrays.asList(
                "科室工作进度汇报", "医疗质量问题复盘", "患者投诉问题分析",
                "排班与人力协调", "院内新规传达学习", "重点病例讨论会诊"
        ));
        java.util.Collections.shuffle(lib);
        int count = Math.min(lib.size(), 3 + (int)(Math.random() * 3));
        return lib.subList(0, count);
    }
}