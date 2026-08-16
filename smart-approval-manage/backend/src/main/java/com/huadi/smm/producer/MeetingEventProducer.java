package com.huadi.smm.producer;

import com.huadi.smm.event.MeetingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class MeetingEventProducer {

    private static final Logger log = LoggerFactory.getLogger(MeetingEventProducer.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void send(MeetingEvent event) {
        try {
            kafkaTemplate.send("meeting.events", String.valueOf(event.getMeetingId()), event);
        } catch (Exception e) {
            log.warn("发送会议事件失败(Kafka 未启用，忽略): type={}, meetingId={}",
                    event.getEventType(), event.getMeetingId());
        }
    }
}