package com.huadi.smm.supervise.controller;

import com.huadi.smm.supervise.dto.Result;
import com.huadi.smm.supervise.entity.AssignRecord;
import com.huadi.smm.supervise.service.AssignService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/supervise/assign")
public class AssignController {

    private final AssignService assignService;

    public AssignController(AssignService assignService) {
        this.assignService = assignService;
    }

    /**
     * AI自动分派
     * POST /api/supervise/assign/auto/{problemId}
     */
    @PostMapping("/auto/{problemId}")
    public Result<AssignRecord> autoAssign(@PathVariable Long problemId) {
        AssignRecord record = assignService.autoAssign(problemId);
        return Result.ok("自动分派成功", record);
    }

    /**
     * 人工改派
     * POST /api/supervise/assign/manual
     * 请求体: {"problemId": 1, "userId": 2, "operatorId": 1, "reason": "任务重新分配"}
     */
    @PostMapping("/manual")
    public Result<Void> manualAssign(@RequestBody Map<String, Object> params) {
        Long problemId = Long.valueOf(params.get("problemId").toString());
        Long userId = Long.valueOf(params.get("userId").toString());
        Long operatorId = Long.valueOf(params.get("operatorId").toString());
        String reason = params.get("reason") != null ? params.get("reason").toString() : null;

        assignService.manualAssign(problemId, userId, operatorId, reason);
        return Result.ok("改派成功", null);
    }

    /**
     * 查询问题的当前负责人
     * GET /api/supervise/assign/current/{problemId}
     */
    @GetMapping("/current/{problemId}")
    public Result<Map<String, Long>> getCurrentAssignee(@PathVariable Long problemId) {
        Long assigneeId = assignService.getCurrentAssignee(problemId);
        Map<String, Long> result = new HashMap<>();
        result.put("assigneeId", assigneeId);
        return Result.ok(result);
    }
}