package com.huadi.smm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class HandleApproveRequest {

    @NotNull(message = "approver_id 不能为空")
    @JsonProperty("approver_id")
    private Long approverId;

    @NotNull(message = "action 不能为空")
    @JsonProperty("action")
    private Integer action;

    @NotBlank(message = "opinion 不能为空")
    @JsonProperty("opinion")
    private String opinion;
}
