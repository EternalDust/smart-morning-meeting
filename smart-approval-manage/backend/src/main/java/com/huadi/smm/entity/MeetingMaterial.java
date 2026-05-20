package com.huadi.smm.entity;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "sm_meeting_material")
public class MeetingMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meeting_id", nullable = false)
    private Long meetingId;

    @Column(name = "material_name", nullable = false)
    private String materialName;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "check_status")
    private Integer checkStatus;

    @Column(name = "check_opinion")
    private String checkOpinion;

    @Column(name = "uploader_id")
    private Long uploaderId;

    @Column(name = "create_time")
    private Date createTime;
}