package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 晨会信息主表（共享表 sm_meeting_info，汇报交互/审批维护）
 */
@Data
@TableName("sm_meeting_info")
public class SmMeetingInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private Integer meetingType;
    private Long deptId;
    private Long hostId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private Integer status;
    private Integer approveStatus;
    private Long creatorId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
