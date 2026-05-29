package com.huadi.smm.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Date;

@Data
public class MeetingCreateRequest {
    @NotBlank(message = "会议标题不能为空")
    private String title;

    private Integer meetingType;
    private Long deptId;
    private Long hostId;
    private Date startTime;
    private Date endTime;
    private String location;
}