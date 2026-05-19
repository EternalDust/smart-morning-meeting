package com.smartmeeting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("meeting_attendee")
public class Attendee {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long meetingId;
    private String userId;
    private Integer roleType;
    private Integer attendStatus;
}
