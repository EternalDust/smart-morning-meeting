package com.huadi.smm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class AiGenerateRequest {

    @NotNull(message = "meeting_id 不能为空")
    @JsonProperty("meeting_id")
    private Long meetingId;

    @NotBlank(message = "dept_name 不能为空")
    @JsonProperty("dept_name")
    private String deptName;

    @NotNull(message = "meeting_type 不能为空")
    @JsonProperty("meeting_type")
    private Integer meetingType;
}
