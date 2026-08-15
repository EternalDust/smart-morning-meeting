package com.huadi.smm.controller;

import com.huadi.smm.config.Result;
import com.huadi.smm.entity.ApproveProcessDef;
import com.huadi.smm.service.ApproveProcessDefService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/process-def")
public class ApproveProcessDefController {

    @Autowired
    private ApproveProcessDefService processDefService;

    @GetMapping
    public Result<List<ApproveProcessDef>> list() {
        return Result.ok(processDefService.list());
    }

    @GetMapping("/{id}")
    public Result<ApproveProcessDef> get(@PathVariable Long id) {
        return Result.ok(processDefService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody ApproveProcessDef def) {
        return Result.ok(processDefService.save(def));
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody ApproveProcessDef def) {
        def.setId(id);
        return Result.ok(processDefService.updateById(def));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.ok(processDefService.removeById(id));
    }
}