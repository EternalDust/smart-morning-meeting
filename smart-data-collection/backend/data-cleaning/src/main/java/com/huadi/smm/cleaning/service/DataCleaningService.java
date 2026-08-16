package com.huadi.smm.cleaning.service;

import java.util.List;
import java.util.Map;

public interface DataCleaningService {

    void triggerCleaning(String type);

    Map<String, Object> getCleaningStats();

    Map<String, Object> getQualityTrend(int days);

    /**
     * 标准数据表明细查询（供可视化/大数据等下游读取）
     * @param page 页码（从 1 开始）
     * @param size 每页条数
     * @param department 科室过滤（可为空）
     */
    Map<String, Object> getCleanDataList(int page, int size, String department);

    /**
     * 按科室聚合的标准数据统计（关联晨会科室维度：department 对应 sm_gm_members.dept）
     */
    List<Map<String, Object>> getCleanDataByDepartment();
}
