package com.huadi.smm.supervise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huadi.smm.supervise.entity.Document;
import com.huadi.smm.supervise.entity.Problem;
import com.huadi.smm.supervise.mapper.DocumentMapper;
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

    @Override
    public String generateDocument(Long problemId, Integer docType) {
        // 1. 查询问题信息
        Problem problem = problemService.getById(problemId);
        if (problem == null) {
            throw new IllegalArgumentException("问题不存在");
        }

        // 2. 根据文书类型生成不同内容
        String content = buildDocumentContent(problem, docType);

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
