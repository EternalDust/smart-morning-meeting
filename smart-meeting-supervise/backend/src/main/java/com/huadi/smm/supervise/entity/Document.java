package com.huadi.smm.supervise.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sm_document")
public class Document {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long problemId;

    private Integer docType;

    private String content;

    private Integer genType;

    private Integer checkStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
