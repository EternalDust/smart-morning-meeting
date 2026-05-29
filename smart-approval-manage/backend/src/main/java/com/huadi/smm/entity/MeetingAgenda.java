package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sm_meeting_agenda")
public class MeetingAgenda {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long meetingId;
    private String title;
    private Long speakerId;
    private Integer duration;
    private Integer sort;
    private Date createTime;
}