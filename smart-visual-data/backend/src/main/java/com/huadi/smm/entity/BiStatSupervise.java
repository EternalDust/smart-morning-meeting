package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("bi_stat_supervise")
public class BiStatSupervise {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String statDate;
    private Long deptId;
    private Integer totalCount;
    private Integer solvedCount;
    private BigDecimal solveRate;
    private Integer overdueCount;
    private Integer urgentCount;
    private Integer avgHandleHour;
    private String createTime;
}
