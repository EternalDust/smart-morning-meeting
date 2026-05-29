package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sm_meeting_info")
public class MeetingInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private Integer meetingType;
    private Long deptId;
    private Long hostId;
    private Date startTime;
    private Date endTime;
    private String location;
    private Integer approveStatus;
    private Long creatorId;
    private Date createTime;
    private Date updateTime;
}