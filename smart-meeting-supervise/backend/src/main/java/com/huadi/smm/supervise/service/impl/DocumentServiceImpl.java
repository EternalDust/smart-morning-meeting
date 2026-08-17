package com.huadi.smm.supervise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huadi.smm.supervise.ai.AiClient;
import com.huadi.smm.supervise.entity.Document;
import com.huadi.smm.supervise.entity.Problem;
import com.huadi.smm.supervise.entity.User;
import com.huadi.smm.supervise.enums.CategoryEnum;
import com.huadi.smm.supervise.mapper.DocumentMapper;
import com.huadi.smm.supervise.mapper.UserMapper;
import com.huadi.smm.supervise.service.DocumentService;
import com.huadi.smm.supervise.service.ProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document>
        implements DocumentService {

    @Autowired
    private ProblemService problemService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AiClient aiClient;

    @Override
    public String generateDocument(Long problemId, Integer docType) {
        // 1. 查询问题信息
        Problem problem = problemService.getById(problemId);
        if (problem == null) {
            throw new IllegalArgumentException("问题不存在");
        }

        // 2. 优先使用大模型生成；失败（未配置 Key / 网络异常）时降级为模板
        String content = aiClient.chat(SYSTEM_PROMPT, buildAiPrompt(problem, docType));
        if (content == null) {
            content = buildDocumentContent(problem, docType);
        }

        // 3. 保存到数据库
        Document document = new Document();
        document.setProblemId(problemId);
        document.setDocType(docType);
        document.setContent(content);
        document.setGenType(1);  // AI生成
        document.setCheckStatus(0);  // 待审核
        this.save(document);

        return content;
    }

    private static final String SYSTEM_PROMPT =
            "你是一名医院质量管理办公室的文书专员，负责撰写规范、正式的中文督办文书。" +
            "只输出文书正文本身，不要输出任何解释、Markdown 标记或多余内容。" +
            "文书要结构清晰、措辞严肃、条理分明。";

    /**
     * 构造大模型的文书生成提示词
     */
    private String buildAiPrompt(Problem problem, Integer docType) {
        String docTypeName;
        String requirements;
        switch (docType) {
            case 1:
                docTypeName = "督办通知书";
                requirements = "包含文书标题、主送对象（责任科室/责任人）、督办事项、督办要求、办理时限、落款单位与日期占位。";
                break;
            case 2:
                docTypeName = "整改通知书";
                requirements = "包含文书标题、主送对象（责任科室/责任人）、问题详情、整改要求、整改期限（自收到通知起7个工作日内）、出具单位与日期占位。";
                break;
            case 3:
                docTypeName = "闭环报告";
                requirements = "包含文书标题、问题标题、整改情况概述、结案意见、审核部门与结案日期。";
                break;
            case 4:
                docTypeName = "催办通知书";
                requirements = "包含文书标题、主送对象（责任科室/责任人）、原督办事项与截止时间、当前逾期/临期情况说明、催办要求、再次明确的办理期限、落款单位与日期占位。";
                break;
            default:
                throw new IllegalArgumentException("不支持的文书类型");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("请根据以下晨会督办问题的信息，生成一份【").append(docTypeName).append("】。\n\n");
        sb.append("问题标题：").append(problem.getTitle()).append("\n");
        sb.append("问题描述：").append(problem.getContent() != null ? problem.getContent() : "无").append("\n");
        sb.append("问题分类：").append(getCategoryDesc(problem.getCategory())).append("\n");
        sb.append("风险等级：").append(getRiskLevelDesc(problem.getRiskLevel())).append("\n");
        if (problem.getDeadline() != null) {
            sb.append("办理截止时间：").append(problem.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
        }
        if (problem.getAssigneeId() != null) {
            User assignee = userMapper.selectById(problem.getAssigneeId());
            if (assignee != null) {
                sb.append("责任人：").append(assignee.getName())
                        .append(assignee.getDept() != null ? "（" + assignee.getDept() + "）" : "")
                        .append("\n");
            }
        }
        sb.append("\n要求：").append(requirements);
        sb.append(" 标题居中，正文分条列出，落款处写“质量管理办公室”和日期占位。");
        return sb.toString();
    }

    private String getCategoryDesc(Integer category) {
        if (category == null) return "未分类";
        for (CategoryEnum e : CategoryEnum.values()) {
            if (e.getCode().equals(category)) return e.getDesc();
        }
        return "未分类";
    }

    /**
     * 构建文书内容
     */
    private String buildDocumentContent(Problem problem, Integer docType) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String docTitle;
        String docContent;

        switch (docType) {
            case 1:
                docTitle = "【督办通知书】";
                docContent = String.format(
                        "督办事项：%s\n\n" +
                                "问题描述：%s\n\n" +
                                "风险等级：%s\n\n" +
                                "督办要求：请相关责任人在规定时间内完成整改，并将整改结果上报督办部门。\n\n" +
                                "生成时间：%s\n" +
                                "督办部门：质量管理办公室",
                        problem.getTitle(),
                        problem.getContent() != null ? problem.getContent() : "无",
                        getRiskLevelDesc(problem.getRiskLevel()),
                        now
                );
                break;
            case 2:
                docTitle = "【整改通知书】";
                docContent = String.format(
                        "整改事项：%s\n\n" +
                                "问题详情：%s\n\n" +
                                "整改要求：请于收到通知后7个工作日内完成整改，并提交整改报告。\n\n" +
                                "出具时间：%s\n" +
                                "出具部门：质控办公室",
                        problem.getTitle(),
                        problem.getContent() != null ? problem.getContent() : "无",
                        now
                );
                break;
            case 3:
                docTitle = "【闭环报告】";
                docContent = String.format(
                        "问题标题：%s\n\n" +
                                "整改情况：已完成整改\n\n" +
                                "结案时间：%s\n\n" +
                                "结案意见：经复查，问题已整改到位，同意结案。\n\n" +
                                "审核部门：质量管理办公室",
                        problem.getTitle(),
                        now
                );
                break;
            case 4:
                docTitle = "【催办通知书】";
                docContent = String.format(
                        "催办事项：%s\n\n" +
                                "问题描述：%s\n\n" +
                                "风险等级：%s\n\n" +
                                "原定截止时间：%s\n\n" +
                                "催办要求：请相关责任人加快整改进度，于收到本通知后3个工作日内反馈最新进展，并确保按期闭环。\n\n" +
                                "生成时间：%s\n" +
                                "督办部门：质量管理办公室",
                        problem.getTitle(),
                        problem.getContent() != null ? problem.getContent() : "无",
                        getRiskLevelDesc(problem.getRiskLevel()),
                        problem.getDeadline() != null
                                ? problem.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                                : "未设置",
                        now
                );
                break;
            default:
                throw new IllegalArgumentException("不支持的文书类型");
        }

        return docTitle + "\n\n" + docContent;
    }

    private String getRiskLevelDesc(Integer riskLevel) {
        if (riskLevel == null) return "未定";
        switch (riskLevel) {
            case 1: return "一般";
            case 2: return "重要";
            case 3: return "紧急";
            default: return "未知";
        }
    }

    @Override
    public boolean auditDocument(Long id, Integer status) {
        Document document = new Document();
        document.setId(id);
        document.setCheckStatus(status);
        return this.updateById(document);
    }

    @Override
    public List<Document> getDocumentsByProblemId(Long problemId) {
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Document::getProblemId, problemId)
                .orderByDesc(Document::getCreateTime);
        return this.list(wrapper);
    }
}
