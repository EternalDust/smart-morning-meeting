package com.huadi.smm.workbench.service;

import java.util.Map;

public interface DashboardService {

    Map<String, Object> getOverview();

    Map<String, Object> getSourceStatus();

    Map<String, Object> getQualityTrend(int days);

    Map<String, Object> getProcessingDelay();
}
