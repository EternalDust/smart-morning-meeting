package com.huadi.smm.controller;

import com.huadi.smm.config.Result;
import com.huadi.smm.entity.MeetingAgendaTemplate;
import com.huadi.smm.service.MeetingAgendaTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agenda-template")
public class MeetingAgendaTemplateController {

    @Autowired
    private MeetingAgendaTemplateService templateService;

    @GetMapping
    public Result<List<MeetingAgendaTemplate>> list() {
        return Result.ok(templateService.list());
    }

    @GetMapping("/{id}")
    public Result<MeetingAgendaTemplate> get(@PathVariable Long id) {
        return Result.ok(templateService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody MeetingAgendaTemplate template) {
        return Result.ok(templateService.save(template));
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody MeetingAgendaTemplate template) {
        template.setId(id);
        return Result.ok(templateService.updateById(template));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.ok(templateService.removeById(id));
    }
}