package com.huadi.smm.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class DeadLetterConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterConsumer.class);

    @KafkaListener(topics = "meeting.events.DLT", groupId = "smm-dlt-group")
    public void onDlt(ConsumerRecord<String, Object> record) {
        log.error("死信补偿: topic={}, key={}, partition={}, value={}",
                record.topic(), record.key(), record.partition(), record.value());
        // 告警、入库、人工介入补偿
    }
}