package com.huadi.smm.collection.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huadi.smm.collection.dao.DataSourceConfigDao;
import com.huadi.smm.collection.dao.RawDataDao;
import com.huadi.smm.collection.service.DataCollectService;
import com.huadi.smm.common.entity.DataSourceConfig;
import com.huadi.smm.common.entity.RawData;
import com.huadi.smm.common.enums.MedicalDataDomain;
import com.huadi.smm.common.exception.BusinessException;
import com.huadi.smm.common.utils.DataContentValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DataCollectServiceImpl implements DataCollectService {

    private static final Logger log = LoggerFactory.getLogger(DataCollectServiceImpl.class);
    private static final String TOPIC = "raw-data-topic";

    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private DataSourceConfigDao dataSourceConfigDao;

    @Autowired
    private RawDataDao rawDataDao;

    private final AtomicLong totalReceived = new AtomicLong(0);
    private final AtomicLong totalSuccess = new AtomicLong(0);
    private final AtomicLong totalFailed = new AtomicLong(0);

    @Override
    public Long sendToKafka(String source, Map<String, Object> data) {
        totalReceived.incrementAndGet();

        // 1. 数据源必须已注册且启用
        LambdaQueryWrapper<DataSourceConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DataSourceConfig::getSourceCode, source)
                .eq(DataSourceConfig::getStatus, 1);
        DataSourceConfig ds = dataSourceConfigDao.selectOne(wrapper);
        if (ds == null) {
            totalFailed.incrementAndGet();
            throw new BusinessException(400, "数据源未注册或已禁用：" + source);
        }

        // 2. 数据域：优先取上报 payload，其次取数据源配置，最后默认 GENERAL
        String domain = pickDataDomain(data, ds);
        if (MedicalDataDomain.fromCode(domain) == null) {
            totalFailed.incrementAndGet();
            throw new BusinessException(400, "数据域不在允许范围内：" + domain
                    + "，允许：HIS/LIS/EMR/PACS/DRUG/MEETING/GENERAL");
        }
        // 数据源若限定了数据域，则不允许上报其他域的数据
        if (StringUtils.hasText(ds.getDataDomain())
                && !ds.getDataDomain().equalsIgnoreCase(domain)) {
            totalFailed.incrementAndGet();
            throw new BusinessException(400, "数据域超出该数据源允许范围：" + domain
                    + "（该数据源限定 " + ds.getDataDomain() + "）");
        }

        // 3. 数据内容校验（必填字段、格式、取值白名单）
        String contentError = DataContentValidator.validateContent(ds.getSourceType(), domain, data);
        if (contentError != null) {
            totalFailed.incrementAndGet();
            throw new BusinessException(400, "数据内容校验失败：" + contentError);
        }

        // 4. 富化字段
        data.put("sourceCode", source);
        data.put("dataDomain", domain);
        data.put("collectTime", LocalDateTime.now().toString());

        // 5. 落库 data_raw_data，得到 rawId
        RawData raw = new RawData();
        raw.setSourceCode(source);
        raw.setDataJson(JSONUtil.toJsonStr(data));
        raw.setCollectTime(LocalDateTime.now());
        rawDataDao.insert(raw);
        Long rawId = raw.getId();

        // 6. 发送 Kafka（演示模式下 Kafka 可能未配置，仅记录日志，不再记为失败）
        if (kafkaTemplate == null) {
            log.warn("KafkaTemplate 未配置，数据已落库 data_raw_data（演示模式）。source: {}, rawId: {}",
                    source, rawId);
            totalSuccess.incrementAndGet();
            return rawId;
        }

        String dataJson = JSONUtil.toJsonStr(data);
        kafkaTemplate.send(TOPIC, source, dataJson)
                .addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                    @Override
                    public void onSuccess(SendResult<String, String> result) {
                        totalSuccess.incrementAndGet();
                        log.info("数据发送Kafka成功, offset: {}, source: {}, rawId: {}",
                                result.getRecordMetadata().offset(), source, rawId);
                    }

                    @Override
                    public void onFailure(Throwable ex) {
                        totalFailed.incrementAndGet();
                        log.error("数据发送Kafka失败, source: {}, rawId: {}", source, rawId, ex);
                    }
                });
        return rawId;
    }

    @Override
    public Map<String, Object> getCollectStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalReceived", totalReceived.get());
        stats.put("totalSuccess", totalSuccess.get());
        stats.put("totalFailed", totalFailed.get());
        stats.put("successRate", totalReceived.get() == 0 ? 0 :
                String.format("%.2f%%", totalSuccess.get() * 100.0 / totalReceived.get()));
        return stats;
    }

    @Override
    public Page<RawData> pageRawData(int page, int size, String sourceCode) {
        Page<RawData> p = new Page<>(page, size);
        LambdaQueryWrapper<RawData> query = new LambdaQueryWrapper<>();
        query.eq(StringUtils.hasText(sourceCode), RawData::getSourceCode, sourceCode)
                .orderByDesc(RawData::getCollectTime);
        return rawDataDao.selectPage(p, query);
    }

    private String pickDataDomain(Map<String, Object> data, DataSourceConfig ds) {
        Object d = data.get("dataDomain");
        if (d == null) {
            d = data.get("domain");
        }
        if (d != null && StringUtils.hasText(d.toString())) {
            return d.toString().toUpperCase();
        }
        if (StringUtils.hasText(ds.getDataDomain())) {
            return ds.getDataDomain().toUpperCase();
        }
        return "GENERAL";
    }
}
