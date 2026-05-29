package com.huadi.smm.supervise.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sm_problem")
public class Problem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private Integer sourceType;

    private Long creatorId;

    private Long assigneeId;

    private Integer category;

    private Integer riskLevel;

    private Integer priority;

    private Integer status;

    private LocalDateTime deadline;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
