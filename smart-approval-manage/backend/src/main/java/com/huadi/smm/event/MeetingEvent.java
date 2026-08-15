package com.huadi.smm.event;

import java.io.Serializable;

public class MeetingEvent implements Serializable {
    private String eventType;
    private Long meetingId;
    private Integer oldStatus;
    private Integer newStatus;
    private Long timestamp;

    public MeetingEvent() {}

    public static MeetingEvent of(String eventType, Long meetingId, Integer oldStatus, Integer newStatus) {
        MeetingEvent e = new MeetingEvent();
        e.eventType = eventType;
        e.meetingId = meetingId;
        e.oldStatus = oldStatus;
        e.newStatus = newStatus;
        e.timestamp = System.currentTimeMillis();
        return e;
    }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Long getMeetingId() { return meetingId; }
    public void setMeetingId(Long meetingId) { this.meetingId = meetingId; }
    public Integer getOldStatus() { return oldStatus; }
    public void setOldStatus(Integer oldStatus) { this.oldStatus = oldStatus; }
    public Integer getNewStatus() { return newStatus; }
    public void setNewStatus(Integer newStatus) { this.newStatus = newStatus; }
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}