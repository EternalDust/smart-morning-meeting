package com.huadi.smm.job;

import com.huadi.smm.entity.MeetingInfo;
import com.huadi.smm.service.MeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class ApproveTimeoutJob {

    @Autowired
    private MeetingService meetingService;

    @Scheduled(fixedRate = 600000)
    public void checkTimeout() {
        List<MeetingInfo> list = meetingService.listAll();
        long now = System.currentTimeMillis();
        for (MeetingInfo m : list) {
            if (m.getApproveStatus() != null && m.getApproveStatus() == 1
                    && m.getCreateTime() != null
                    && (now - m.getCreateTime().getTime() > 86400000)) {
                System.out.println("[超时预警] meetingId=" + m.getId() + " 审批超时");
            }
        }
    }
}