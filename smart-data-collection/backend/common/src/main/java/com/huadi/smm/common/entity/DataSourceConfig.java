package com.huadi.smm.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("data_source_config")
public class DataSourceConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sourceCode;
    private String sourceName;
    private String sourceType;
    private String configJson;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
