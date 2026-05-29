package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sm_meeting_attendee")
public class MeetingAttendee {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long meetingId;
    private Long userId;
    private Integer roleType;
    private Integer attendStatus;
    private Date inviteTime;
}