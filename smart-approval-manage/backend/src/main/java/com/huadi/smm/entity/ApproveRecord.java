package com.huadi.smm.entity;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "sm_approve_record")
public class ApproveRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meeting_id", nullable = false)
    private Long meetingId;

    @Column(name = "process_id")
    private Long processId;

    @Column(name = "node_name")
    private String nodeName;

    @Column(name = "approver_id")
    private Long approverId;

    private Integer action;

    private String opinion;

    @Column(name = "approve_time")
    private Date approveTime;
}