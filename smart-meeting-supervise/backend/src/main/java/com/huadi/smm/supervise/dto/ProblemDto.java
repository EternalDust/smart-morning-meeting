package com.huadi.smm.supervise.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ProblemDto {
    @NotBlank(message = "问题标题不能为空")
    private String title;

    private String content;

    private Integer sourceType;

    private Long creatorId;

    private Long assigneeId;

    private Integer category;

    private Integer riskLevel;

    private Integer priority;

    private String deadline;
}
