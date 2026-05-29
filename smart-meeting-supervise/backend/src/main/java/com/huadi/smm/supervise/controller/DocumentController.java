package com.huadi.smm.supervise.controller;

import com.huadi.smm.supervise.dto.Result;
import com.huadi.smm.supervise.entity.Document;
import com.huadi.smm.supervise.service.DocumentService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/supervise/document")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * 生成文书
     * POST /api/supervise/document/generate
     * 请求体: {"problemId": 1, "docType": 1}
     * docType: 1督办单 2整改通知书 3闭环报告
     */
    @PostMapping("/generate")
    public Result<Map<String, String>> generateDocument(@RequestBody Map<String, Object> params) {
        Long problemId = Long.valueOf(params.get("problemId").toString());
        Integer docType = Integer.valueOf(params.get("docType").toString());

        String content = documentService.generateDocument(problemId, docType);

        Map<String, String> result = new HashMap<>();
        result.put("content", content);
        result.put("message", "文书生成成功");
        return Result.ok(result);
    }

    /**
     * 审核文书
     * POST /api/supervise/document/audit/{id}
     * 请求体: {"status": 1}
     * status: 1通过 2驳回
     */
    @PostMapping("/audit/{id}")
    public Result<Void> auditDocument(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer status = Integer.valueOf(params.get("status").toString());
        documentService.auditDocument(id, status);
        String msg = status == 1 ? "审核通过" : "审核驳回";
        return Result.ok(msg, null);
    }

    /**
     * 查询问题的所有文书
     * GET /api/supervise/document/list/{problemId}
     */
    @GetMapping("/list/{problemId}")
    public Result<List<Document>> getDocumentsByProblemId(@PathVariable Long problemId) {
        List<Document> list = documentService.getDocumentsByProblemId(problemId);
        return Result.ok(list);
    }
}