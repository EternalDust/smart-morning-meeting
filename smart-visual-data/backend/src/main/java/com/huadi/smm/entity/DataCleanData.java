package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 清洗后数据表（共享表 data_clean_data，采集清洗后写入）
 * 用于大屏展示医疗质量评分等指标。
 */
@Data
@TableName("data_clean_data")
public class DataCleanData {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String patientId;
    private LocalDateTime visitTime;
    private Integer age;
    private String gender;
    private String diagnosis;
    private String department;
    private String doctorId;
    /** 质量评分（0-100） */
    private BigDecimal qualityScore;
    private LocalDateTime createTime;
}
