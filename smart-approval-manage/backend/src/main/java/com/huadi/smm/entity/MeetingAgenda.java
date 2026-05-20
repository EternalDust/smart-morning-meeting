package com.huadi.smm.entity;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "meeting_agenda")
public class MeetingAgenda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meeting_id", nullable = false)
    private Long meetingId;

    @Column(nullable = false)
    private String title;

    @Column(name = "speaker_id")
    private Long speakerId;

    private Integer duration;

    private Integer sort;

    @Column(name = "create_time")
    private Date createTime;
}