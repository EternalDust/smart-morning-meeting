package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 晨会发言转写表（共享表 sm_meeting_speech，汇报交互维护）
 */
@Data
@TableName("sm_meeting_speech")
public class SmMeetingSpeech {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long meetingId;
    /** 发言人 ID（工号） */
    private String speakerId;
    private String content;
    private String speechTime;
    private String keyPoints;
}
