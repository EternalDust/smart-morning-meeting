package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("sm_meeting_analytics_weekly")
public class WeeklyTrend {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String meetingWeek;
    private BigDecimal avgAttendRate;
    private BigDecimal avgQualityScore;
    private Integer totalSpeeches;
    private Integer totalInteractions;
    private Integer meetingCount;
    private String createdAt;
}
