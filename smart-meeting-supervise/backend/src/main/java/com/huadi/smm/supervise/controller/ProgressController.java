package com.huadi.smm.supervise.controller;

import com.huadi.smm.supervise.dto.Result;
import com.huadi.smm.supervise.entity.ProgressRecord;
import com.huadi.smm.supervise.service.ProgressService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/supervise/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    /**
     * 上报进度
     * POST /api/supervise/progress/submit
     */
    @PostMapping("/submit")
    public Result<Void> submitProgress(@RequestBody Map<String, Object> params) {
        ProgressRecord record = new ProgressRecord();
        record.setProblemId(Long.valueOf(params.get("problemId").toString()));
        record.setProgress(Integer.valueOf(params.get("progress").toString()));
        record.setRemark(params.get("remark") != null ? params.get("remark").toString() : null);
        record.setReporterId(params.get("reporterId") != null ?
                Long.valueOf(params.get("reporterId").toString()) : null);

        progressService.submitProgress(record);
        return Result.success("进度上报成功", null);
    }

    /**
     * 查询进度历史
     * GET /api/supervise/progress/history/{problemId}
     */
    @GetMapping("/history/{problemId}")
    public Result<List<ProgressRecord>> getHistory(@PathVariable Long problemId) {
        List<ProgressRecord> list = progressService.getProgressHistory(problemId);
        return Result.success(list);
    }

    /**
     * 查询当前进度
     * GET /api/supervise/progress/current/{problemId}
     */
    @GetMapping("/current/{problemId}")
    public Result<Map<String, Object>> getCurrentProgress(@PathVariable Long problemId) {
        Integer progress = progressService.getCurrentProgress(problemId);
        Map<String, Object> result = new HashMap<>();
        result.put("problemId", problemId);
        result.put("progress", progress);
        return Result.success(result);
    }
}