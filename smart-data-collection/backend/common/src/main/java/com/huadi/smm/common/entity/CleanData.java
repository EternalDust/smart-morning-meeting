package com.huadi.smm.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("clean_data")
public class CleanData {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String patientId;
    private LocalDateTime visitTime;
    private Integer age;
    private String gender;
    private String diagnosis;
    private String department;
    private String doctorId;
    private BigDecimal qualityScore;
    private LocalDateTime createTime;
}
