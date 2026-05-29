package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sm_meeting_summary")
public class MeetingSummary {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long meetingId;
    private String summary;
    private String createTime;
}
