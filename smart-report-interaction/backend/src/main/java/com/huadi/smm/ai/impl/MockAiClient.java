package com.huadi.smm.ai.impl;

import com.huadi.smm.ai.AiClient;
import com.huadi.smm.ai.dto.SpeechLine;
import com.huadi.smm.ai.dto.SummaryResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MockAiClient implements AiClient {

    private static final List<String> MEDICAL_TERMS = Arrays.asList(
            "高血压", "糖尿病", "冠心病", "肺炎", "感染", "发热", "疼痛", "头晕", "恶心",
            "心率", "血压", "血糖", "阿司匹林", "胰岛素", "抗生素", "阿莫西林", "降压药",
            "CT", "核磁", "超声", "心电图", "血常规", "影像", "化验",
            "外科", "内科", "儿科", "急诊", "手术", "床位", "复查", "转诊", "会诊", "护理"
    );

    private static final Pattern DECISION_PATTERN =
            Pattern.compile("[^。；;]+(?:明天|后续|需要|安排|跟进|复查|督办|尽快)[^。；;]*[。；;]?");

    @Override
    public SummaryResult generateSummary(String meetingTitle, List<SpeechLine> speeches) {
        SummaryResult result = new SummaryResult();
        if (speeches == null || speeches.isEmpty()) {
            result.setSummary("当前会议暂无发言记录");
            return result;
        }
        StringBuilder summary = new StringBuilder();
        summary.append("本次晨会主题《").append(meetingTitle == null || meetingTitle.isEmpty() ? "未填写" : meetingTitle)
                .append("》，共").append(speeches.size()).append("位成员发言：");
        for (int i = 0; i < speeches.size(); i++) {
            SpeechLine s = speeches.get(i);
            summary.append(i > 0 ? "；" : "").append(s.getSpeaker()).append("汇报").append(truncate(s.getContent(), 30));
        }
        summary.append("。");
        result.setSummary(summary.toString());

        List<String> keyPoints = new ArrayList<>();
        Set<String> decisions = new LinkedHashSet<>();
        Set<String> entities = new LinkedHashSet<>();
        for (SpeechLine s : speeches) {
            keyPoints.add(s.getSpeaker() + "：" + truncate(s.getContent(), 40));
            Matcher dm = DECISION_PATTERN.matcher(s.getContent() == null ? "" : s.getContent());
            while (dm.find()) {
                decisions.add(dm.group().trim());
            }
            for (String term : MEDICAL_TERMS) {
                if (s.getContent() != null && s.getContent().contains(term)) {
                    entities.add(term);
                }
            }
        }
        if (keyPoints.size() > 6) {
            keyPoints = new ArrayList<>(keyPoints.subList(0, 6));
        }
        result.setKeyPoints(keyPoints);
        result.setDecisions(new ArrayList<>(decisions));
        result.setMedicalEntities(new ArrayList<>(entities));
        return result;
    }

    @Override
    public String answerQuestion(String question, List<SpeechLine> context) {
        String q = question == null ? "" : question;
        if (q.contains("血压") || q.contains("高血压")) {
            return "根据晨会信息，建议对高血压患者每日监测血压并记录，调整降压药方案前请心内科会诊，必要时安排门诊复查。";
        }
        if (q.contains("血糖") || q.contains("糖尿病")) {
            return "糖尿病患者需注意规律饮食与用药，建议内分泌科评估血糖控制情况，必要时调整用药剂量并加强健康宣教。";
        }
        if (q.contains("床位")) {
            return "床位紧张问题建议由护理部汇总各科室空床情况，按急诊优先原则动态调配，并于明日晨会通报结果。";
        }
        if (q.contains("手术") || q.contains("排期")) {
            return "手术排期建议结合各科室上报情况，急症手术优先处理，择期手术由医务科统一协调，尽快排定并通知相关科室。";
        }
        if (q.contains("感染") || q.contains("发热")) {
            return "涉及发热或感染病例需按院感防控流程处置，及时上报感控科并做好隔离观察，同时对相关区域加强消杀。";
        }
        if (context != null && !context.isEmpty()) {
            return "结合本次晨会发言，相关科室已就此事进行通报，建议由责任科室会后牵头跟进，并在下次晨会反馈进展。";
        }
        return "该问题暂未有直接对应的晨会信息，建议结合医疗规范流程处理，必要时组织相关科室专题会商。";
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        String t = text.replaceAll("\\s+", "");
        return t.length() <= max ? t : t.substring(0, max) + "…";
    }
}
