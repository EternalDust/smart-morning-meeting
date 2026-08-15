package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("sm_department_analytics")
public class DepartmentAnalytics {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String department;
    private Integer meetingCount;
    private BigDecimal avgAttendRate;
    private Integer totalSpeechCount;
    private Integer totalInteractionCount;
    private String periodStart;
    private String periodEnd;
    private String updatedAt;
}
