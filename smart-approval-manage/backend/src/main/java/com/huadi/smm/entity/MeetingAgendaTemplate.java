package com.huadi.smm.entity;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "sm_meeting_agenda_template")
public class MeetingAgendaTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_name", nullable = false)
    private String templateName;

    @Column(name = "dept_id")
    private Long deptId;

    @Column(columnDefinition = "text")
    private String content;

    @Column(name = "creator_id")
    private Long creatorId;

    private Integer status;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;
}