package com.huadi.smm.cleaning.service;

import java.util.Map;

public interface DataCleaningService {

    void triggerCleaning(String type);

    Map<String, Object> getCleaningStats();

    Map<String, Object> getQualityTrend(int days);
}
