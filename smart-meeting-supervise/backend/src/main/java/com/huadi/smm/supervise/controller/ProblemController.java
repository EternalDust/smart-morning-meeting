package com.huadi.smm.supervise.controller;

import com.huadi.smm.supervise.dto.Result;
import com.huadi.smm.supervise.entity.Problem;
import com.huadi.smm.supervise.service.ProblemService;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/supervise/problem")
public class ProblemController {

    private final ProblemService problemService;

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
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
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
}
