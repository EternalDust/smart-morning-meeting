package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 参会人员表（共享表 sm_meeting_attendee）
 */
@Data
@TableName("sm_meeting_attendee")
public class SmMeetingAttendee {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long meetingId;
    private Long userId;
    /** 参会角色：1 主持 2 汇报 3 列席 */
    private Integer roleType;
    /** 参会状态：0 已邀请 1 已确认 */
    private Integer attendStatus;
    private LocalDateTime inviteTime;
}
