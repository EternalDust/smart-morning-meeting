package com.smartmeeting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sm_meeting_speech")
public class SpeechRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long meetingId;
    private String speakerId;
    private String content;
    private String speechTime;
    private String keyPoints;
}
