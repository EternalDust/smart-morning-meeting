package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("bi_warn_record")
public class BiWarnRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String warnType;
    private Integer warnLevel;
    private Long deptId;
    private String indexCode;
    private String abnormalValue;
    private String thresholdValue;
    private Integer status;
    private Long handlerId;
    private String handleTime;
    private String createTime;
}
