package com.smartmeeting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sm_meeting_interaction")
public class Interaction {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long meetingId;
    private String userId;
    private Integer interactType;
    private String content;
    private String reply;
    private String createTime;
}
