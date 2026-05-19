package com.smartmeeting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sm_meeting_signin")
public class SignIn {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long meetingId;
    private String userId;
    private String signTime;
    private Integer signType;
    private Integer signStatus;
}
