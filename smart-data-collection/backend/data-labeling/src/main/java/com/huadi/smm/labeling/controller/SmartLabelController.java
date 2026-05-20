package com.huadi.smm.labeling.controller;

import com.huadi.smm.common.entity.R;
import com.huadi.smm.labeling.service.SmartLabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/label")
public class SmartLabelController {

    @Autowired
    private SmartLabelService smartLabelService;

    @PostMapping("/generate")
    public R generateLabels(@RequestParam String entityType) {
        smartLabelService.generateLabels(entityType);
        return R.ok().message("标签生成任务已启动，目标类型：" + entityType);
    }

    @GetMapping("/list")
    public R listLabels(@RequestParam(defaultValue = "doctor") String entityType) {
        return R.ok(smartLabelService.listLabelsByType(entityType));
    }

    @PostMapping("/nlp/parse")
    public R parseUnstructuredText(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        String recordType = body.getOrDefault("recordType", "morning_meeting");
        Map<String, Object> parsed = smartLabelService.parseUnstructuredText(text, recordType);
        return R.ok(parsed);
    }

    @PostMapping("/nlp/batch-parse")
    public R batchParse(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> texts = (List<String>) body.get("texts");
        String recordType = body.getOrDefault("recordType", "morning_meeting").toString();
        return R.ok(smartLabelService.batchParse(texts, recordType));
    }
}
