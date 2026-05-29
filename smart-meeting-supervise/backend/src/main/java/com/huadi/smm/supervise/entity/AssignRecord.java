package com.huadi.smm.supervise.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sm_supervise_assign")
public class AssignRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long problemId;

    private Long userId;

    private Integer assignType;  // 1自动 2人工

    private Long operatorId;

    private String reason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
