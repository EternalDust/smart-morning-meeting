package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 晨会签到表（共享表 sm_meeting_signin，汇报交互维护）
 */
@Data
@TableName("sm_meeting_signin")
public class SmMeetingSignin {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long meetingId;
    /** 签到用户 ID（工号） */
    private String userId;
    private String signTime;
    /** 签到方式 */
    private Integer signType;
    /** 签到状态：0 正常 1 迟到 */
    private Integer signStatus;
}
