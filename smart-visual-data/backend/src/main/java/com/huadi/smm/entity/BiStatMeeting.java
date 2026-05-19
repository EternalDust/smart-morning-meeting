package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 晨会统计指标事实表
 */
@Data
@TableName("bi_stat_meeting")
public class BiStatMeeting {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String statDate;
    private Long deptId;
    private String deptName;
    private Integer meetingCount;
    private Integer shouldNum;
    private Integer realNum;
    private BigDecimal attendRate;
    private Integer lateNum;
    private Integer absentNum;
    private Integer avgDuration;
    private String createTime;
}
