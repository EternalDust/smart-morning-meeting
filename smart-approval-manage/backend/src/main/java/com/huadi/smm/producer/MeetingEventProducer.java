package com.huadi.smm.producer;

import com.huadi.smm.event.MeetingEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class MeetingEventProducer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void send(MeetingEvent event) {
        kafkaTemplate.send("meeting.events", String.valueOf(event.getMeetingId()), event);
    }
}