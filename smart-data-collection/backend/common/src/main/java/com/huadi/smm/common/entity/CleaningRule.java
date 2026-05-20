package com.huadi.smm.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("cleaning_rule")
public class CleaningRule {

    @TableId(type = IdType.AUTO)
    private Long ruleId;

    private String ruleName;
    private String ruleType;
    private String ruleConfig;
    private Integer priority;
    private Integer enabled;
}
