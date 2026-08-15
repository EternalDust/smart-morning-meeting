package com.huadi.smm.collection.controller;

import com.huadi.smm.common.entity.R;
import com.huadi.smm.collection.service.DataCollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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
        Long rawId = dataCollectService.sendToKafka(source, data);
        return R.ok(Collections.singletonMap("rawId", rawId))
                .message("数据校验通过，已写入 data_raw_data（rawId=" + rawId + "）");
    }

    @PostMapping("/manual")
    public R collectManualData(@RequestBody Map<String, Object> data,
                               @RequestHeader("X-Source") String source) {
        Long rawId = dataCollectService.sendToKafka(source, data);
        return R.ok(Collections.singletonMap("rawId", rawId))
                .message("手动录入数据已提交（rawId=" + rawId + "）");
    }

    @GetMapping("/status")
    public R getCollectStatus() {
        return R.ok(dataCollectService.getCollectStats());
    }

    @GetMapping("/raw/list")
    public R listRawData(@RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size,
                         @RequestParam(required = false) String sourceCode) {
        return R.ok(dataCollectService.pageRawData(page, size, sourceCode));
    }
}
