package com.huadi.smm.supervise.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sm_supervise_progress")
public class ProgressRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long problemId;

    private Integer progress;

    private String remark;

    private String attachmentUrl;

    private Long reporterId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
