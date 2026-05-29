package com.huadi.smm.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("data_smart_tag")
public class SmartTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String entityType;
    private String entityId;
    private String tagName;
    private String tagLevel;
    private String tagRule;
    private LocalDateTime createTime;
}
