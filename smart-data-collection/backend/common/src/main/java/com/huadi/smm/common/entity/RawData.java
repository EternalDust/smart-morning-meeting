package com.huadi.smm.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("raw_data")
public class RawData {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sourceCode;
    private String dataJson;
    private LocalDateTime collectTime;
    private Long kafkaOffset;
}
