package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sm_meeting_agenda_template")
public class MeetingAgendaTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String templateName;
    private Long deptId;
    private String content;
    private Long creatorId;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}