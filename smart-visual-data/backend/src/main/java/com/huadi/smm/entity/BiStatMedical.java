package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("bi_stat_medical")
public class BiStatMedical {
    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate statDate;
    private Long deptId;
    private String indexCode;
    private String indexName;
    private BigDecimal indexValue;
    private BigDecimal targetValue;
    private BigDecimal completeRate;
    private String createTime;
}
