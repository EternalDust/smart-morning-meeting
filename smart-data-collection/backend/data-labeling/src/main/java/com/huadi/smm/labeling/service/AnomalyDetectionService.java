package com.huadi.smm.labeling.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huadi.smm.common.entity.AnomalyRecord;

import java.util.Map;

public interface AnomalyDetectionService {

    Map<String, Object> detectAnomalies(Map<String, Object> data);

    Page<AnomalyRecord> listAnomalies(int page, int size, String level);

    Map<String, Object> getAnomalyStats();

    void handleAnomaly(Long id, String handler, String remark);
}
