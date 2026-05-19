package com.smartmeeting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sm_meeting_info")
public class MeetingInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String hostId;
    private String startTime;
    private String endTime;
    private Integer status;
    private String createTime;
}
