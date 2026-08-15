package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 大模型晨会复盘报告表 bi_review_report
 * 保存大模型生成的复盘报告 / 管理决策建议
 */
@Data
@TableName("bi_review_report")
public class BiReviewReport {

    public static final int STATUS_SUCCESS = 1;    // 真实大模型生成
    public static final int STATUS_SIMULATED = 2;  // 模板模拟生成（未配置模型或调用失败降级）
    public static final int STATUS_FAILED = 0;     // 生成失败

    public static final String TYPE_REVIEW = "REVIEW"; // 晨会复盘报告
    public static final String TYPE_ADVICE = "ADVICE"; // 管理决策建议

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 报告标题 */
    private String title;

    /** 报告内容（Markdown） */
    private String content;

    /** 报告类型：REVIEW 复盘报告 / ADVICE 决策建议 */
    private String reportType;

    /** 统计开始日期 */
    private String startDate;

    /** 统计结束日期 */
    private String endDate;

    /** 生成状态：1 成功 2 模拟生成 0 失败 */
    private Integer status;

    /** 生成人 */
    private String createBy;

    /** 生成时间 */
    private String createTime;
}
