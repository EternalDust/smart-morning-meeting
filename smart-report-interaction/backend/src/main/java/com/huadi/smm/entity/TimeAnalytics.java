package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("sm_time_analytics")
public class TimeAnalytics {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String period;
    private Long normalCount;
    private Long lateCount;
    private Long total;
    private BigDecimal punctualRate;
    private String updatedAt;
}
