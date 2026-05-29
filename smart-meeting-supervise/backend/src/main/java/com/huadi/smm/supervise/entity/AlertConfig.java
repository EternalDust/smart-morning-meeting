package com.huadi.smm.supervise.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("sm_supervise_alert_config")
public class AlertConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer thresholdHour;

    private Integer channel;

    private Integer status;
}
