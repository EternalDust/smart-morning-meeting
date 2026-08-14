package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("sm_meeting_analytics")
public class MeetingAnalytics {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long meetingId;
    private String meetingTitle;
    private String meetingDate;
    private Integer shouldAttend;
    private Integer actualAttend;
    private BigDecimal attendRate;
    private Integer normalCount;
    private Integer lateCount;
    private Integer speechCount;
    private Integer interactionCount;
    private BigDecimal qualityScore;
    private Integer isAnomaly;
    private String createdAt;
    private String updatedAt;
}
