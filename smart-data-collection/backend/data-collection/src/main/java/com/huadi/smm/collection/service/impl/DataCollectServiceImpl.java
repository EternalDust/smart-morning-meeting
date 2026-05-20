package com.huadi.smm.collection.service.impl;

import cn.hutool.json.JSONUtil;
import com.huadi.smm.collection.service.DataCollectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DataCollectServiceImpl implements DataCollectService {

    private static final Logger log = LoggerFactory.getLogger(DataCollectServiceImpl.class);
    private static final String TOPIC = "raw-data-topic";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final AtomicLong totalReceived = new AtomicLong(0);
    private final AtomicLong totalSuccess = new AtomicLong(0);
    private final AtomicLong totalFailed = new AtomicLong(0);

    @Override
    public void sendToKafka(String source, Map<String, Object> data) {
        totalReceived.incrementAndGet();

        data.put("source", source);
        data.put("collectTime", LocalDateTime.now().toString());
        String dataJson = JSONUtil.toJsonStr(data);

        kafkaTemplate.send(TOPIC, source, dataJson)
                .addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                    @Override
                    public void onSuccess(SendResult<String, String> result) {
                        totalSuccess.incrementAndGet();
                        log.info("数据发送Kafka成功, offset: {}, source: {}",
                                result.getRecordMetadata().offset(), source);
                    }

                    @Override
                    public void onFailure(Throwable ex) {
                        totalFailed.incrementAndGet();
                        log.error("数据发送Kafka失败, source: {}", source, ex);
                    }
                });
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
}
