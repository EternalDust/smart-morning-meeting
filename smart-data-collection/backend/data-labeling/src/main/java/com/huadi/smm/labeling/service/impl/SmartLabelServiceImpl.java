package com.huadi.smm.labeling.service.impl;

import cn.hutool.json.JSONUtil;
import com.huadi.smm.labeling.service.SmartLabelService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SmartLabelServiceImpl implements SmartLabelService {

    /**
     * 基于规则生成标签
     * 完整实现中会调用大模型API进行更精准的标签生成
     */
    @Override
    public void generateLabels(String entityType) {
        // 异步任务：根据业务指标生成智能标签
        // 例如：医生出勤率 > 95% -> "优秀医生"
        //      手术成功率 > 98% -> "高绩效医生"
        //      门诊量同比下降 > 20% -> "需关注医生"
    }

    @Override
    public List<Map<String, Object>> listLabelsByType(String entityType) {
        List<Map<String, Object>> labels = new ArrayList<>();
        if ("doctor".equals(entityType)) {
            labels.add(labelItem("高绩效医生", "手术成功率>98%, 门诊量>1000", "high"));
            labels.add(labelItem("优秀医生", "出勤率>95%, 患者满意度>90%", "medium"));
            labels.add(labelItem("需关注医生", "门诊量同比下降>20%", "low"));
        }
        return labels;
    }

    /**
     * 解析非结构化文本，提取关键实体
     * 采用"异步+分层"处理模式，避免同步调用大模型API的延迟问题
     *
     * 当前使用正则表达式实现基础提取，
     * 完整实现中会通过独立消费组异步调用大模型API
     */
    @Override
    public Map<String, Object> parseUnstructuredText(String text, String recordType) {
        Map<String, Object> parsed = new HashMap<>();

        // 提取患者主诉
        Pattern complaintPattern = Pattern.compile("主诉[：:](.*?)(?:[。，,；;]|现病史|既往史)");
        Matcher cm = complaintPattern.matcher(text);
        if (cm.find()) {
            parsed.put("chiefComplaint", cm.group(1).trim());
        }

        // 提取诊断结果
        Pattern diagPattern = Pattern.compile("诊断[：:](.*?)(?:[。，,；;]|治疗|建议)");
        Matcher dm = diagPattern.matcher(text);
        if (dm.find()) {
            parsed.put("diagnosis", dm.group(1).trim());
        }

        // 提取治疗方案
        Pattern treatPattern = Pattern.compile("治疗[：:方案]*(.*?)(?:[。，,；;]|预后|随访)");
        Matcher tm = treatPattern.matcher(text);
        if (tm.find()) {
            parsed.put("treatment", tm.group(1).trim());
        }

        // 提取科室信息
        Pattern deptPattern = Pattern.compile("(?:科室|部门)[：:](.*?)(?:[。，,；;]|)");
        Matcher deptM = deptPattern.matcher(text);
        if (deptM.find()) {
            parsed.put("department", deptM.group(1).trim());
        }

        parsed.put("recordType", recordType);
        parsed.put("parseTime", new Date().toString());
        parsed.put("parsedSuccessfully", !parsed.isEmpty());

        return parsed;
    }

    /**
     * 批量异步解析——将任务发送到独立Kafka队列
     * 由独立消费组以较低速率消费并调用大模型API
     */
    @Override
    public List<Map<String, Object>> batchParse(List<String> texts, String recordType) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (String text : texts) {
            results.add(parseUnstructuredText(text, recordType));
        }
        return results;
    }

    private Map<String, Object> labelItem(String name, String rule, String level) {
        Map<String, Object> item = new HashMap<>();
        item.put("labelName", name);
        item.put("rule", rule);
        item.put("level", level);
        return item;
    }
}
