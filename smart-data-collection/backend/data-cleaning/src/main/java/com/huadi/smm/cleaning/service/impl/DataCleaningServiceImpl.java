package com.huadi.smm.cleaning.service.impl;

import com.huadi.smm.cleaning.service.DataCleaningService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataCleaningServiceImpl implements DataCleaningService {

    private long totalCleanedRecords = 0;
    private long totalDedupedRecords = 0;
    private long totalFilledRecords = 0;
    private long totalAnomalyRecords = 0;

    @Override
    public void triggerCleaning(String type) {
        // 在实际部署中，这里会通过SparkLauncher或命令行启动Spark作业
        // Spark作业: DataCleaningJob
    }

    @Override
    public Map<String, Object> getCleaningStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCleanedRecords", totalCleanedRecords);
        stats.put("totalDedupedRecords", totalDedupedRecords);
        stats.put("totalFilledRecords", totalFilledRecords);
        stats.put("totalAnomalyRecords", totalAnomalyRecords);
        return stats;
    }

    @Override
    public Map<String, Object> getQualityTrend(int days) {
        Map<String, Object> trend = new HashMap<>();
        List<Double> scores = Arrays.asList(87.5, 88.2, 86.9, 90.1, 89.3, 91.0, 90.5);
        LocalDate today = LocalDate.now();
        for (int i = 0; i < Math.min(days, scores.size()); i++) {
            trend.put(today.minusDays(days - 1 - i).format(DateTimeFormatter.ISO_LOCAL_DATE),
                    scores.get(i));
        }
        return trend;
    }
}
