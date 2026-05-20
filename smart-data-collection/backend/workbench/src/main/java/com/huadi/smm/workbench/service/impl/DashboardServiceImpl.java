package com.huadi.smm.workbench.service.impl;

import com.huadi.smm.workbench.service.DashboardService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new HashMap<>();
        overview.put("totalDataSourceCount", 8);
        overview.put("activeDataSourceCount", 6);
        overview.put("todayDataVolume", 12580);
        overview.put("averageQualityScore", 88.5);
        overview.put("anomalyRecordCount", 12);
        overview.put("systemUptime", "99.97%");

        // 各数据源类型分布
        Map<String, Integer> sourceTypeDistribution = new LinkedHashMap<>();
        sourceTypeDistribution.put("MySQL", 4);
        sourceTypeDistribution.put("HTTP API", 2);
        sourceTypeDistribution.put("Kafka", 1);
        sourceTypeDistribution.put("MongoDB", 1);
        overview.put("sourceTypeDistribution", sourceTypeDistribution);

        return overview;
    }

    @Override
    public Map<String, Object> getSourceStatus() {
        List<Map<String, Object>> sources = new ArrayList<>();
        sources.add(sourceItem(1, "HIS门诊业务库", "mysql", "connected", "2026-05-20 10:30"));
        sources.add(sourceItem(2, "LIS检验系统", "mysql", "connected", "2026-05-20 10:30"));
        sources.add(sourceItem(3, "电子病历系统", "http", "connected", "2026-05-20 10:28"));
        sources.add(sourceItem(4, "PACS影像系统", "mysql", "disconnected", "2026-05-19 15:00"));
        sources.add(sourceItem(5, "药品管理系统", "mysql", "connected", "2026-05-20 10:31"));
        sources.add(sourceItem(6, "前端上报接口", "http", "connected", "2026-05-20 10:32"));

        Map<String, Object> result = new HashMap<>();
        result.put("sources", sources);
        result.put("totalCount", sources.size());
        result.put("connectedCount", sources.stream().filter(s -> "connected".equals(s.get("status"))).count());
        return result;
    }

    @Override
    public Map<String, Object> getQualityTrend(int days) {
        Map<String, Object> trend = new LinkedHashMap<>();
        // 模拟近7天质量评分趋势
        double[] scores = {87.5, 88.2, 86.9, 90.1, 89.3, 91.0, 90.5};
        String[] dates = {"05-14", "05-15", "05-16", "05-17", "05-18", "05-19", "05-20"};
        for (int i = 0; i < Math.min(days, scores.length); i++) {
            trend.put(dates[i], scores[i]);
        }
        return trend;
    }

    @Override
    public Map<String, Object> getProcessingDelay() {
        Map<String, Object> delay = new HashMap<>();
        delay.put("采集服务平均延迟", "3.2秒");
        delay.put("清洗服务平均延迟", "8分钟");
        delay.put("标签化服务平均延迟", "2.5秒");
        delay.put("API平均响应时间", "156ms");
        return delay;
    }

    private Map<String, Object> sourceItem(int id, String name, String type, String status, String lastSync) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", id);
        item.put("sourceName", name);
        item.put("sourceType", type);
        item.put("status", status);
        item.put("lastSyncTime", lastSync);
        return item;
    }
}
