package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("sm_member_analytics")
public class MemberAnalytics {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String userId;
    private String userName;
    private String department;
    private Integer totalMeetings;
    private Integer attendedCount;
    private BigDecimal attendRate;
    private Integer normalCount;
    private Integer lateCount;
    private Integer speechCount;
    private Integer interactionCount;
    private String lastUpdated;
}
