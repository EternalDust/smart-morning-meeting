package com.huadi.smm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huadi.smm.dto.MeetingCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AgendaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testListMeetings() throws Exception {
        mockMvc.perform(get("/api/agenda/meetings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testCreateMeeting() throws Exception {
        MeetingCreateRequest req = new MeetingCreateRequest();
        req.setTitle("Controller测试会议");
        req.setMeetingType(1);              // 改成 Integer
        req.setDeptId(1L);
        req.setHostId(1L);
        req.setStartTime(new Date());       // 改成 Date
        req.setEndTime(new Date(System.currentTimeMillis() + 3600000));
        req.setLocation("测试会议室");

        mockMvc.perform(post("/api/agenda/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Controller测试会议"));
    }

    @Test
    public void testGetMeetingDetail() throws Exception {
        mockMvc.perform(get("/api/agenda/meetings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}