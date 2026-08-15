package com.huadi.smm.service;

import com.huadi.smm.llm.LlmClient;
import com.huadi.smm.util.DesensitizeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LlmAuditService {

    @Autowired
    private LlmClient llmClient;

    public String auditMaterial(String materialName, String rawContent) {
        String safeName = DesensitizeUtil.desensitize(materialName);
        String safeContent = DesensitizeUtil.desensitize(rawContent);

        String system =
                "你是一名会议材料合规审查专家。请检查材料中是否存在敏感信息泄露、违规内容，并给出简要预审意见。";
        String user = "材料名称：" + safeName + "\n内容：\n" + safeContent + "\n\n请给出合规检查结果及预审意见。";

        return llmClient.chat(system, user);
    }

    public String generatePreOpinion(String meetingTitle, String materialSummary) {
        String safeTitle = DesensitizeUtil.desensitize(meetingTitle);
        String safeSummary = DesensitizeUtil.desensitize(materialSummary);

        String system = "你是一名会议审批专家。请根据会议主题和材料摘要生成一段简短的预审意见，用于辅助领导决策。";
        String user = "会议主题：" + safeTitle + "\n材料摘要：" + safeSummary + "\n\n请生成预审意见。";

        return llmClient.chat(system, user);
    }
}