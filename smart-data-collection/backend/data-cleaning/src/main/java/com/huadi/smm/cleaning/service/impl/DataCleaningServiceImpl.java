package com.huadi.smm.cleaning.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.huadi.smm.cleaning.dao.CleanDataDao;
import com.huadi.smm.cleaning.service.DataCleaningService;
import com.huadi.smm.collection.dao.RawDataDao;
import com.huadi.smm.common.entity.CleanData;
import com.huadi.smm.common.entity.RawData;
import com.huadi.smm.common.utils.DataQualityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataCleaningServiceImpl implements DataCleaningService {

    @Autowired
    private RawDataDao rawDataDao;

    @Autowired
    private CleanDataDao cleanDataDao;

    private long totalCleanedRecords = 0;
    private long totalDedupedRecords = 0;
    private long totalFilledRecords = 0;
    private long totalAnomalyRecords = 0;

    @Override
    public void triggerCleaning(String type) {
        // 演示模式：内存清洗——读 data_raw_data → 去重/标准化/空值填充/质量评分 → 写 data_clean_data
        // 生产环境此处替换为 Spark 作业（DataCleaningJob），清洗口径保持一致
        List<RawData> rawList = rawDataDao.selectList(null);
        long cleaned = 0, deduped = 0, filled = 0, anomalies = 0;
        Map<String, Boolean> seen = new HashMap<>();

        if (rawList != null) {
            for (RawData raw : rawList) {
                try {
                    JSONObject obj = JSONUtil.parseObj(raw.getDataJson());
                    String patientId = obj.getStr("patientId");
                    String visitTime = obj.getStr("visitTime");
                    if (patientId == null || visitTime == null) {
                        continue;
                    }
                    // 1. 去重：同一 patientId + visitTime 仅保留首条
                    String key = patientId + "|" + visitTime;
                    if (seen.put(key, Boolean.TRUE) != null) {
                        deduped++;
                        continue;
                    }
                    CleanData clean = new CleanData();
                    clean.setPatientId(patientId);
                    clean.setVisitTime(parseVisitTime(visitTime));

                    // 2. 空值填充：age → -1、diagnosis/gender → 未知
                    Object age = obj.get("age");
                    if (age == null || String.valueOf(age).trim().isEmpty()) {
                        clean.setAge(-1);
                        filled++;
                    } else {
                        try {
                            clean.setAge(Integer.parseInt(String.valueOf(age).trim()));
                        } catch (NumberFormatException e) {
                            clean.setAge(-1);
                            filled++;
                        }
                    }
                    String gender = obj.getStr("gender");
                    if (gender == null || gender.trim().isEmpty()) {
                        clean.setGender("未知");
                        filled++;
                    } else {
                        clean.setGender(standardizeGender(gender));
                    }
                    String diagnosis = obj.getStr("diagnosis");
                    if (diagnosis == null || diagnosis.trim().isEmpty()) {
                        clean.setDiagnosis("未知");
                        filled++;
                    } else {
                        clean.setDiagnosis(diagnosis);
                    }
                    clean.setDepartment(obj.getStr("department"));
                    clean.setDoctorId(obj.getStr("doctorId"));

                    // 3. 质量评分（完整性40% + 一致性30% + 有效性30%）
                    double score = DataQualityUtil.calculateQualityScore(buildQualityMap(clean));
                    clean.setQualityScore(BigDecimal.valueOf(score));
                    clean.setCreateTime(LocalDateTime.now());

                    cleanDataDao.insert(clean);
                    cleaned++;
                } catch (Exception e) {
                    anomalies++;
                }
            }
        }

        totalCleanedRecords = cleaned;
        totalDedupedRecords = deduped;
        totalFilledRecords = filled;
        totalAnomalyRecords = anomalies;
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

    /** 就诊时间解析：支持 yyyy-MM-dd HH:mm:ss 与 yyyy-MM-dd，解析失败抛异常计入异常 */
    private LocalDateTime parseVisitTime(String visitTime) {
        String s = visitTime.trim();
        if (s.length() == 10) {
            return LocalDate.parse(s).atStartOfDay();
        }
        return LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /** 性别编码标准化：M/m/1 → 男，F/f/0 → 女，中文保持原样 */
    private String standardizeGender(String gender) {
        String g = gender.trim();
        if ("M".equalsIgnoreCase(g) || "1".equals(g)) return "男";
        if ("F".equalsIgnoreCase(g) || "0".equals(g)) return "女";
        return g;
    }

    /** 构造参与质量评分的字段集合（评分对空值/占位符敏感） */
    private Map<String, Object> buildQualityMap(CleanData clean) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("patientId", clean.getPatientId());
        m.put("visitTime", clean.getVisitTime() == null ? null : clean.getVisitTime().toString());
        m.put("age", clean.getAge());
        m.put("gender", clean.getGender());
        m.put("diagnosis", clean.getDiagnosis());
        m.put("department", clean.getDepartment());
        m.put("doctorId", clean.getDoctorId());
        return m;
    }

    @Override
    public Map<String, Object> getCleanDataList(int page, int size, String department) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<CleanData> all = cleanDataDao.selectList(
                department == null || department.trim().isEmpty() ? null
                        : new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CleanData>()
                                .eq(CleanData::getDepartment, department.trim()));
        if (all == null) {
            all = new java.util.ArrayList<>();
        }
        int total = all.size();
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(total, from + size);
        List<CleanData> pageList = from >= total ? new java.util.ArrayList<>() : all.subList(from, to);

        List<Map<String, Object>> rows = new java.util.ArrayList<>();
        for (CleanData c : pageList) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", c.getId());
            row.put("patientId", c.getPatientId());
            row.put("visitTime", c.getVisitTime() == null ? null : c.getVisitTime().toString());
            row.put("age", c.getAge());
            row.put("gender", c.getGender());
            row.put("diagnosis", c.getDiagnosis());
            row.put("department", c.getDepartment());
            row.put("doctorId", c.getDoctorId());
            row.put("qualityScore", c.getQualityScore());
            row.put("createTime", c.getCreateTime() == null ? null : c.getCreateTime().toString());
            rows.add(row);
        }
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("list", rows);
        return result;
    }

    @Override
    public List<Map<String, Object>> getCleanDataByDepartment() {
        List<CleanData> all = cleanDataDao.selectList(null);
        Map<String, Integer> countMap = new LinkedHashMap<>();
        Map<String, BigDecimal> scoreSum = new LinkedHashMap<>();
        if (all != null) {
            for (CleanData c : all) {
                String dept = c.getDepartment() == null || c.getDepartment().trim().isEmpty()
                        ? "未知" : c.getDepartment().trim();
                countMap.put(dept, countMap.getOrDefault(dept, 0) + 1);
                scoreSum.put(dept, scoreSum.getOrDefault(dept, BigDecimal.ZERO)
                        .add(c.getQualityScore() == null ? BigDecimal.ZERO : c.getQualityScore()));
            }
        }
        List<Map<String, Object>> list = new java.util.ArrayList<>();
        for (Map.Entry<String, Integer> e : countMap.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("department", e.getKey());
            item.put("count", e.getValue());
            item.put("avgQualityScore", scoreSum.get(e.getKey())
                    .divide(BigDecimal.valueOf(e.getValue()), 2, java.math.RoundingMode.HALF_UP));
            list.add(item);
        }
        return list;
    }
}
