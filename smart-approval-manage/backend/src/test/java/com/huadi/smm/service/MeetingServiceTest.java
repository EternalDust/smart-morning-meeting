package com.huadi.smm.service;

import com.huadi.smm.entity.MeetingInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MeetingServiceTest {

    @Autowired
    private MeetingService meetingService;

    @Test
    public void testSaveMeeting() {
        MeetingInfo meeting = new MeetingInfo();
        meeting.setTitle("单元测试会议");
        meeting.setMeetingType(1);
        meeting.setDeptId(1L);
        meeting.setHostId(1L);
        meeting.setStartTime(new Date());
        meeting.setEndTime(new Date(System.currentTimeMillis() + 3600000));
        meeting.setLocation("会议室A");

        MeetingInfo result = meetingService.save(meeting);
        assertNotNull(result.getId());
        System.out.println("生成会议ID: " + result.getId());
    }

    @Test
    public void testGetById() {
        MeetingInfo meeting = meetingService.getById(1L);
        assertNotNull(meeting);
        System.out.println("会议标题: " + meeting.getTitle());
    }

    @Test
    public void testListAll() {
        List<MeetingInfo> list = meetingService.listAll();
        assertNotNull(list);
        System.out.println("会议总数: " + list.size());
    }

    @Test
    public void testArchiveMeeting() {
        // 先保存一个已通过(approveStatus=2)的会议，才能归档
        MeetingInfo meeting = new MeetingInfo();
        meeting.setTitle("归档测试会议");
        meeting.setMeetingType(1);
        meeting.setDeptId(1L);
        meeting.setHostId(1L);
        meeting.setStartTime(new Date());
        meeting.setEndTime(new Date(System.currentTimeMillis() + 3600000));
        meeting.setLocation("测试会议室");
        meeting.setApproveStatus(2);  // 手动设为已通过

        MeetingInfo saved = meetingService.save(meeting);
        Long id = saved.getId();
        assertNotNull(id);

        boolean result = meetingService.archiveMeeting(id);
        assertTrue(result);

        MeetingInfo updated = meetingService.getById(id);
        assertEquals(4, updated.getApproveStatus());
        System.out.println("归档成功，会议ID: " + id);
    }
}