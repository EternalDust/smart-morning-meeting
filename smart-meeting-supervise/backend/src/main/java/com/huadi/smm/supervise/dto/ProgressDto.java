package com.huadi.smm.supervise.dto;

import lombok.Data;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class ProgressDto {
    @NotNull(message = "问题ID不能为空")
    private Long problemId;

    @Min(value = 0, message = "进度不能小于0")
    @Max(value = 100, message = "进度不能大于100")
    private Integer progress;

    private String remark;

    private String attachmentUrl;
}
