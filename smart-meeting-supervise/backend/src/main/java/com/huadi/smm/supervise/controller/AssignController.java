package com.huadi.smm.supervise.controller;

import com.huadi.smm.supervise.dto.Result;
import com.huadi.smm.supervise.entity.AssignRecord;
import com.huadi.smm.supervise.entity.User;
import com.huadi.smm.supervise.mapper.UserMapper;
import com.huadi.smm.supervise.service.AssignService;
import com.huadi.smm.supervise.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/supervise/assign")
public class AssignController {

    private final AssignService assignService;

    @Autowired
    private UserMapper userMapper;

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
        Long problemId = params.get("problemId") != null ? Long.valueOf(params.get("problemId").toString()) : null;
        Long userId = params.get("userId") != null ? Long.valueOf(params.get("userId").toString()) : null;
        Long operatorId = params.get("operatorId") != null ? Long.valueOf(params.get("operatorId").toString()) : null;
        String reason = params.get("reason") != null ? params.get("reason").toString() : null;

        if (problemId == null || userId == null) {
            throw new IllegalArgumentException("problemId 和 userId 不能为空");
        }
        assignService.manualAssign(problemId, userId, operatorId, reason);
        return Result.ok("改派成功", null);
    }

    /**
     * 查询问题的当前负责人
     * GET /api/supervise/assign/current/{problemId}
     */
    @GetMapping("/current/{problemId}")
    public Result<Map<String, Object>> getCurrentAssignee(@PathVariable Long problemId) {
        Long assigneeId = assignService.getCurrentAssignee(problemId);
        Map<String, Object> result = new HashMap<>();
        result.put("assigneeId", assigneeId);
        if (assigneeId != null) {
            User user = userMapper.selectById(assigneeId);
            result.put("assigneeName", user != null ? user.getName() : null);
        }
        return Result.ok(result);
    }

    /**
     * 查询可分配的执行责任人
     * GET /api/supervise/assign/users
     */
    @GetMapping("/users")
    public Result<List<UserVo>> listAssignableUsers() {
        return Result.ok(assignService.listAssignableUsers());
    }
}
