package com.huadi.smm.labeling.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huadi.smm.common.entity.AnomalyRecord;
import com.huadi.smm.common.exception.BusinessException;
import com.huadi.smm.labeling.dao.AnomalyRecordDao;
import com.huadi.smm.labeling.service.AnomalyDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnomalyDetectionServiceImpl implements AnomalyDetectionService {

    @Autowired
    private AnomalyRecordDao anomalyRecordDao;

    /**
     * 基于统计规则的异常检测
     * 在完整实现中，此方法会调用Python训练的孤立森林模型API
     */
    @Override
    public Map<String, Object> detectAnomalies(Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        int anomalyCount = 0;

        // 规则1: 门诊量异常检测（阈值：日均变化超过30%视为异常）
        if (data.containsKey("outpatientVolume")) {
            double volume = Double.parseDouble(data.get("outpatientVolume").toString());
            double baseline = Double.parseDouble(data.getOrDefault("baselineVolume", "100").toString());
            if (Math.abs(volume - baseline) / baseline > 0.3) {
                saveAnomaly("门诊量", String.valueOf(volume),
                        baseline * 0.7 + " ~ " + baseline * 1.3,
                        "high", "门诊量较基线偏离超过30%");
                anomalyCount++;
            }
        }

        // 规则2: 药占比异常检测（超过50%视为异常）
        if (data.containsKey("drugRatio")) {
            double ratio = Double.parseDouble(data.get("drugRatio").toString());
            if (ratio > 0.5) {
                saveAnomaly("药占比", String.format("%.2f%%", ratio * 100),
                        "≤ 50%", "medium", "药占比超过50%阈值");
                anomalyCount++;
            }
        }

        // 规则3: 手术成功率异常检测（低于95%触发预警）
        if (data.containsKey("surgerySuccessRate")) {
            double rate = Double.parseDouble(data.get("surgerySuccessRate").toString());
            if (rate < 0.95) {
                saveAnomaly("手术成功率", String.format("%.1f%%", rate * 100),
                        "≥ 95%", "high", "手术成功率低于95%标准");
                anomalyCount++;
            }
        }

        result.put("checkedCount", 3);
        result.put("anomalyCount", anomalyCount);
        result.put("anomalyRate", String.format("%.1f%%", anomalyCount * 100.0 / 3));
        return result;
    }

    private void saveAnomaly(String indicator, String value, String expected,
                             String level, String desc) {
        AnomalyRecord record = new AnomalyRecord();
        record.setIndicatorName(indicator);
        record.setIndicatorValue(value);
        record.setExpectedRange(expected);
        record.setAnomalyLevel(level);
        record.setDescription(desc);
        record.setStatus(0); // 0=未处理
        record.setDetectTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        anomalyRecordDao.insert(record);
    }

    @Override
    public Page<AnomalyRecord> listAnomalies(int page, int size, String level) {
        Page<AnomalyRecord> p = new Page<>(page, size);
        LambdaQueryWrapper<AnomalyRecord> wrapper = new LambdaQueryWrapper<>();
        if (level != null && !level.isEmpty()) {
            wrapper.eq(AnomalyRecord::getAnomalyLevel, level);
        }
        wrapper.orderByDesc(AnomalyRecord::getDetectTime);
        return anomalyRecordDao.selectPage(p, wrapper);
    }

    @Override
    public Map<String, Object> getAnomalyStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", anomalyRecordDao.selectCount(null));
        stats.put("unhandledCount",
                anomalyRecordDao.selectCount(new LambdaQueryWrapper<AnomalyRecord>()
                        .eq(AnomalyRecord::getStatus, 0)));
        return stats;
    }

    @Override
    public void handleAnomaly(Long id, String handler, String remark) {
        AnomalyRecord record = anomalyRecordDao.selectById(id);
        if (record == null) {
            throw new BusinessException(404, "异常记录不存在");
        }
        record.setStatus(1);
        anomalyRecordDao.updateById(record);
    }
}
