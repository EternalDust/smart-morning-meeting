package com.huadi.smm.service;

import com.huadi.smm.dao.MeetingInfoRepository;
import com.huadi.smm.entity.MeetingInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MeetingService {

    @Autowired
    private MeetingInfoRepository meetingInfoRepository;

    public List<MeetingInfo> listAll() {
        return meetingInfoRepository.findAll();
    }

    public MeetingInfo getById(Long id) {
        return meetingInfoRepository.findById(id).orElse(null);
    }
}
