package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 问题信息主表（共享表 sm_problem，督办维护）
 * category：1 医疗 2 运维 3 管理
 */
@Data
@TableName("sm_problem")
public class SmProblem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String content;
    /** 来源：1 自动采集 2 手动录入 */
    private Integer sourceType;
    /** 录入人 ID（sm_gm_members.id） */
    private Long creatorId;
    /** 负责人 ID（sm_gm_members.id） */
    private Long assigneeId;
    private Integer category;
    private Integer riskLevel;
    private Integer priority;
    /** 状态：0 待分派 1 处理中 2 待复查 3 已结案 */
    private Integer status;
    private LocalDateTime deadline;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
