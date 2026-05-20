package com.huadi.smm.entity;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "meeting_attendee")
public class MeetingAttendee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meeting_id", nullable = false)
    private Long meetingId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "role_type")
    private Integer roleType;

    @Column(name = "attend_status")
    private Integer attendStatus;

    @Column(name = "invite_time")
    private Date inviteTime;
}