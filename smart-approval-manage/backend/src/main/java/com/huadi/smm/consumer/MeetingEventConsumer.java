package com.huadi.smm.consumer;

import com.huadi.smm.event.MeetingEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class MeetingEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(MeetingEventConsumer.class);

    @KafkaListener(topics = "meeting.events", groupId = "smm-consumer-group")
    public void onEvent(ConsumerRecord<String, Object> record) {
        MeetingEvent event = (MeetingEvent) record.value();
        log.info("消费事件: type={}, meetingId={}", event.getEventType(), event.getMeetingId());
        // 跨子系统同步、发送通知等业务处理
    }
}