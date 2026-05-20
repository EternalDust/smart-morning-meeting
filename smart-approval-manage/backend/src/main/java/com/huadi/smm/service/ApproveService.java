package com.huadi.smm.service;

import com.huadi.smm.dao.ApproveRecordRepository;
import com.huadi.smm.dao.MeetingInfoRepository;
import com.huadi.smm.entity.ApproveRecord;
import com.huadi.smm.entity.MeetingInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class ApproveService {

    @Autowired
    private MeetingInfoRepository meetingInfoRepository;

    @Autowired
    private ApproveRecordRepository approveRecordRepository;

    @Transactional
    public boolean submitApprove(Long meetingId) {
        MeetingInfo meeting = meetingInfoRepository.findById(meetingId).orElse(null);
        if (meeting == null || meeting.getApproveStatus() == null || meeting.getApproveStatus() != 0) {
            return false;
        }
        meeting.setApproveStatus(1);
        meetingInfoRepository.save(meeting);
        return true;
    }

    @Transactional
    public boolean handleApprove(Long meetingId, Long approverId, Integer action, String opinion) {
        if (action == null || (action != 1 && action != 2)) {
            return false;
        }
        MeetingInfo meeting = meetingInfoRepository.findById(meetingId).orElse(null);
        if (meeting == null || meeting.getApproveStatus() == null || meeting.getApproveStatus() != 1) {
            return false;
        }
        ApproveRecord record = new ApproveRecord();
        record.setMeetingId(meetingId);
        record.setApproverId(approverId);
        record.setAction(action);
        record.setOpinion(opinion);
        record.setApproveTime(new Date());
        approveRecordRepository.save(record);

        meeting.setApproveStatus(action == 1 ? 2 : 3);
        meetingInfoRepository.save(meeting);
        return true;
    }
}