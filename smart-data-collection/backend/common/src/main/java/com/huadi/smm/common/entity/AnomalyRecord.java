package com.huadi.smm.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("data_anomaly_record")
public class AnomalyRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String indicatorName;
    private String indicatorValue;
    private String expectedRange;
    private String anomalyLevel;
    private String description;
    private Integer status;
    private LocalDateTime detectTime;
    private LocalDateTime createTime;
}
