package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sm_meeting_material")
public class MeetingMaterial {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long meetingId;
    private String materialName;
    private String fileUrl;
    private Long fileSize;
    private Integer checkStatus;
    private String checkOpinion;
    private Long uploaderId;
    private Date createTime;
}