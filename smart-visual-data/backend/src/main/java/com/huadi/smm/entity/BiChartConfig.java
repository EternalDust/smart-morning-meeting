package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("bi_chart_config")
public class BiChartConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String chartCode;
    private String chartName;
    private String chartType;
    private String sourceTable;
    private String roleIds;
    private Integer enabled;
    private Integer sort;
    private String updateTime;
}
