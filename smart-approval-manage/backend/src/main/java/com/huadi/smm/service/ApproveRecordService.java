package com.huadi.smm.service;

import com.huadi.smm.dao.ApproveRecordMapper;
import com.huadi.smm.entity.ApproveRecord;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApproveRecordService {

    @Autowired
    private ApproveRecordMapper approveRecordMapper;

    public List<ApproveRecord> listByMeetingId(Long meetingId) {
        QueryWrapper<ApproveRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("meeting_id", meetingId).orderByDesc("approve_time");
        return approveRecordMapper.selectList(wrapper);
    }
}