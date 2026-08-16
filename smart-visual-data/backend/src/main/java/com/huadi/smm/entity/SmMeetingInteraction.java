package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 晨会交互表（共享表 sm_meeting_interaction，汇报交互维护）
 * interact_type：1 提问 2 反馈 3 投票
 */
@Data
@TableName("sm_meeting_interaction")
public class SmMeetingInteraction {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long meetingId;
    private String userId;
    private Integer interactType;
    private String content;
    private String reply;
    private String createTime;
}
