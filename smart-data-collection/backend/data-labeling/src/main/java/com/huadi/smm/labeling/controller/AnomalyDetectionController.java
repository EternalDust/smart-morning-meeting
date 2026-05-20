package com.huadi.smm.labeling.controller;

import com.huadi.smm.common.entity.R;
import com.huadi.smm.labeling.service.AnomalyDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/anomaly")
public class AnomalyDetectionController {

    @Autowired
    private AnomalyDetectionService anomalyDetectionService;

    @PostMapping("/detect")
    public R detectAnomalies(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> result = anomalyDetectionService.detectAnomalies(requestData);
        return R.ok(result);
    }

    @GetMapping("/list")
    public R listAnomalies(@RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(required = false) String level) {
        return R.ok(anomalyDetectionService.listAnomalies(page, size, level));
    }

    @GetMapping("/stats")
    public R getAnomalyStats() {
        return R.ok(anomalyDetectionService.getAnomalyStats());
    }

    @PutMapping("/{id}/handle")
    public R handleAnomaly(@PathVariable Long id, @RequestBody Map<String, String> body) {
        anomalyDetectionService.handleAnomaly(id, body.get("handler"), body.get("remark"));
        return R.ok().message("异常记录已处理");
    }
}
