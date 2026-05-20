package com.huadi.smm.entity;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "sm_meeting_info")
public class MeetingInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "meeting_type")
    private Integer meetingType;

    @Column(name = "dept_id")
    private Long deptId;

    @Column(name = "host_id")
    private Long hostId;

    @Column(name = "start_time")
    private Date startTime;

    @Column(name = "end_time")
    private Date endTime;

    private String location;

    @Column(name = "approve_status")
    private Integer approveStatus;

    @Column(name = "creator_id")
    private Long creatorId;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;
}