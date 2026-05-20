package com.huadi.smm.collection.controller;

import com.huadi.smm.common.entity.R;
import com.huadi.smm.collection.service.DataCollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/collect")
public class DataCollectController {

    @Autowired
    private DataCollectService dataCollectService;

    @PostMapping("/report")
    public R collectReportData(@RequestBody Map<String, Object> data,
                               @RequestHeader(value = "X-Source", required = false) String source) {
        if (source == null || source.isEmpty()) {
            return R.error(400, "数据源标识(X-Source)不能为空");
        }
        dataCollectService.sendToKafka(source, data);
        return R.ok().message("数据已接收，正在处理中");
    }

    @PostMapping("/manual")
    public R collectManualData(@RequestBody Map<String, Object> data,
                               @RequestHeader("X-Source") String source) {
        dataCollectService.sendToKafka(source, data);
        return R.ok().message("手动录入数据已提交");
    }

    @GetMapping("/status")
    public R getCollectStatus() {
        return R.ok(dataCollectService.getCollectStats());
    }
}
