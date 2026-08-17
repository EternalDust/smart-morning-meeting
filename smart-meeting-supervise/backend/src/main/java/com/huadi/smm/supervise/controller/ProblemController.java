package com.huadi.smm.supervise.controller;

import com.huadi.smm.supervise.dto.Result;
import com.huadi.smm.supervise.entity.Problem;
import com.huadi.smm.supervise.entity.User;
import com.huadi.smm.supervise.mapper.UserMapper;
import com.huadi.smm.supervise.service.MeetingImportService;
import com.huadi.smm.supervise.service.ProblemService;
import com.huadi.smm.supervise.utils.CurrentUser;
import com.huadi.smm.supervise.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/supervise/problem")
public class ProblemController {

    private final ProblemService problemService;

    @Autowired
    private MeetingImportService meetingImportService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CurrentUser currentUser;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @PostMapping("/add")
    public Result<Long> addProblem(@Valid @RequestBody Problem problem) {
        Long id = problemService.addProblem(problem);
        return Result.ok(id);
    }

    @GetMapping("/detail/{id}")
    public Result<Problem> getDetail(@PathVariable Long id) {
        Problem problem = problemService.getById(id);
        if (problem == null) {
            return Result.fail(404, "问题不存在");
        }
        return Result.ok(problem);
    }

    @GetMapping("/list")
    public Result<Object> list(@RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(problemService.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size)));
    }

    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status,
                                     HttpServletRequest request) {
        if (!currentUser.isAdmin(request, null)) {
            throw new IllegalArgumentException("仅督办专员（管理员）可变更问题状态");
        }
        problemService.updateStatus(id, status);
        return Result.ok(null);
    }

    /**
     * 设置截止时间
     * PUT /api/supervise/problem/deadline/{id}?deadline=2026-08-20 18:00:00
     */
    @PutMapping("/deadline/{id}")
    public Result<Void> updateDeadline(@PathVariable Long id, @RequestParam String deadline) {
        LocalDateTime parsed;
        try {
            parsed = LocalDateTime.parse(deadline, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            throw new IllegalArgumentException("截止时间格式应为 yyyy-MM-dd HH:mm:ss");
        }
        problemService.updateDeadline(id, parsed);
        return Result.ok(null);
    }

    /**
     * 从会议导入问题（对接汇报交互读接口）
     * POST /api/supervise/problem/import-meeting?meetingId=1
     */
    @PostMapping("/import-meeting")
    public Result<Map<String, Object>> importFromMeeting(@RequestParam Long meetingId, HttpServletRequest request) {
        User user = currentUser.resolve(request, null);
        return Result.ok(meetingImportService.importFromMeeting(meetingId, user != null ? user.getId() : null));
    }

    /**
     * 我的任务：当前执行责任人负责的处理中/待复查问题
     * GET /api/supervise/problem/mine?account=1001（或从 JWT 的 sub 取工号）
     */
    @GetMapping("/mine")
    public Result<Map<String, Object>> myProblems(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) String account) {
        String userId = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            try {
                userId = jwtUtils.getClaimsFromToken(authorization.substring(7)).getSubject();
            } catch (Exception ignored) {
            }
        }
        if (userId == null) {
            userId = account;
        }
        if (userId == null) {
            throw new IllegalArgumentException("缺少身份信息，请先登录");
        }
        User user = userMapper.findByUserId(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        List<Problem> problems = problemService.listMyProblems(user.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("account", user.getUserId());
        result.put("name", user.getName());
        result.put("problems", problems);
        return Result.ok(result);
    }

    /**
     * 复查通过，闭环问题（仅管理员）
     * POST /api/supervise/problem/close/{id}
     */
    @PostMapping("/close/{id}")
    public Result<Void> closeProblem(@PathVariable Long id, HttpServletRequest request) {
        if (!currentUser.isAdmin(request, null)) {
            throw new IllegalArgumentException("仅督办专员（管理员）可执行闭环审核");
        }
        problemService.closeProblem(id);
        return Result.ok("复查通过，问题已闭环", null);
    }

    /**
     * 删除问题（仅管理员，级联删除分派/进度/文书）
     * DELETE /api/supervise/problem/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProblem(@PathVariable Long id, HttpServletRequest request) {
        if (!currentUser.isAdmin(request, null)) {
            throw new IllegalArgumentException("仅督办专员（管理员）可删除问题");
        }
        problemService.deleteProblem(id);
        return Result.ok("删除成功", null);
    }
}
