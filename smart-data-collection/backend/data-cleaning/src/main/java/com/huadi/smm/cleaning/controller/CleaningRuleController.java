package com.huadi.smm.cleaning.controller;

import com.huadi.smm.cleaning.service.CleaningRuleService;
import com.huadi.smm.common.entity.CleaningRule;
import com.huadi.smm.common.entity.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cleaning-rule")
public class CleaningRuleController {

    @Autowired
    private CleaningRuleService cleaningRuleService;

    @PostMapping("/add")
    public R addRule(@RequestBody CleaningRule rule) {
        cleaningRuleService.addRule(rule);
        return R.ok().message("清洗规则添加成功");
    }

    @GetMapping("/list")
    public R listRules() {
        return R.ok(cleaningRuleService.listAll());
    }

    @PutMapping("/{ruleId}")
    public R updateRule(@PathVariable Long ruleId, @RequestBody CleaningRule rule) {
        rule.setRuleId(ruleId);
        cleaningRuleService.updateRule(rule);
        return R.ok().message("清洗规则更新成功");
    }

    @DeleteMapping("/{ruleId}")
    public R deleteRule(@PathVariable Long ruleId) {
        cleaningRuleService.deleteRule(ruleId);
        return R.ok().message("清洗规则删除成功");
    }

    @PostMapping("/{ruleId}/toggle")
    public R toggleRule(@PathVariable Long ruleId) {
        cleaningRuleService.toggleRule(ruleId);
        return R.ok().message("规则启停切换成功");
    }
}
