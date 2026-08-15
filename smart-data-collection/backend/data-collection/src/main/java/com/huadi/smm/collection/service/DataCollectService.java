package com.huadi.smm.collection.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huadi.smm.common.entity.RawData;

import java.util.Map;

public interface DataCollectService {

    /**
     * 校验并提交一条采集数据。校验通过后落库 data_raw_data（返回 rawId），
     * 若 KafkaTemplate 可用则异步发送到 Kafka。
     *
     * @param source 数据源编号（source_code）
     * @param data   上报数据
     * @return 落库后的 rawId
     */
    Long sendToKafka(String source, Map<String, Object> data);

    Map<String, Object> getCollectStats();

    Page<RawData> pageRawData(int page, int size, String sourceCode);
}
