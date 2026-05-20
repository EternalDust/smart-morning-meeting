package com.huadi.smm.entity;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "sm_approve_process_def")
public class ApproveProcessDef {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "process_name", nullable = false)
    private String processName;

    @Column(name = "dept_id")
    private Long deptId;

    @Column(columnDefinition = "text")
    private String nodesJson;

    private Integer status;

    @Column(name = "create_time")
    private Date createTime;
}