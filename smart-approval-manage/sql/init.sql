USE smart_meeting;

CREATE TABLE IF NOT EXISTS sm_meeting_info (
                                               id bigint NOT NULL AUTO_INCREMENT,
                                               title varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
                                               meeting_type int DEFAULT NULL,
                                               dept_id bigint DEFAULT NULL,
                                               host_id bigint DEFAULT NULL,
                                               start_time datetime DEFAULT NULL,
                                               end_time datetime DEFAULT NULL,
                                               location varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
                                               approve_status int DEFAULT NULL,
                                               creator_id bigint DEFAULT NULL,
                                               create_time datetime DEFAULT NULL,
                                               update_time datetime DEFAULT NULL,
                                               PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 规范：加 sm_ 前缀
CREATE TABLE IF NOT EXISTS sm_meeting_agenda (
                                                 id bigint NOT NULL AUTO_INCREMENT,
                                                 meeting_id bigint NOT NULL,
                                                 title varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
                                                 speaker_id bigint DEFAULT NULL,
                                                 duration int DEFAULT NULL,
                                                 sort int DEFAULT NULL,
                                                 create_time datetime DEFAULT NULL,
                                                 PRIMARY KEY (id),
                                                 KEY meeting_id (meeting_id),
                                                 CONSTRAINT sm_meeting_agenda_ibfk_1 FOREIGN KEY (meeting_id) REFERENCES sm_meeting_info (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 规范：加 sm_ 前缀
CREATE TABLE IF NOT EXISTS sm_meeting_attendee (
                                                   id bigint NOT NULL AUTO_INCREMENT,
                                                   meeting_id bigint NOT NULL,
                                                   user_id bigint NOT NULL,
                                                   role_type int DEFAULT NULL,
                                                   attend_status int DEFAULT NULL,
                                                   invite_time datetime DEFAULT NULL,
                                                   PRIMARY KEY (id),
                                                   KEY meeting_id (meeting_id),
                                                   CONSTRAINT sm_meeting_attendee_ibfk_1 FOREIGN KEY (meeting_id) REFERENCES sm_meeting_info (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS sm_meeting_material (
                                                   id bigint NOT NULL AUTO_INCREMENT,
                                                   meeting_id bigint NOT NULL,
                                                   material_name varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
                                                   file_url varchar(512) COLLATE utf8mb4_general_ci DEFAULT NULL,
                                                   file_size bigint DEFAULT NULL,
                                                   check_status int DEFAULT NULL,
                                                   check_opinion varchar(512) COLLATE utf8mb4_general_ci DEFAULT NULL,
                                                   uploader_id bigint DEFAULT NULL,
                                                   create_time datetime DEFAULT NULL,
                                                   PRIMARY KEY (id),
                                                   KEY meeting_id (meeting_id),
                                                   CONSTRAINT sm_meeting_material_ibfk_1 FOREIGN KEY (meeting_id) REFERENCES sm_meeting_info (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS sm_meeting_agenda_template (
                                                          id bigint NOT NULL AUTO_INCREMENT,
                                                          template_name varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
                                                          dept_id bigint DEFAULT NULL,
                                                          content text COLLATE utf8mb4_general_ci,
                                                          creator_id bigint DEFAULT NULL,
                                                          status int DEFAULT NULL,
                                                          create_time datetime DEFAULT NULL,
                                                          update_time datetime DEFAULT NULL,
                                                          PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS sm_approve_process_def (
                                                      id bigint NOT NULL AUTO_INCREMENT,
                                                      process_name varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
                                                      dept_id bigint DEFAULT NULL,
                                                      nodes_json text COLLATE utf8mb4_general_ci,
                                                      status int DEFAULT NULL,
                                                      create_time datetime DEFAULT NULL,
                                                      PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS sm_approve_record (
                                                 id bigint NOT NULL AUTO_INCREMENT,
                                                 meeting_id bigint NOT NULL,
                                                 process_id bigint DEFAULT NULL,
                                                 node_name varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
                                                 approver_id bigint DEFAULT NULL,
                                                 action int DEFAULT NULL,
                                                 opinion varchar(512) COLLATE utf8mb4_general_ci DEFAULT NULL,
                                                 approve_time datetime DEFAULT NULL,
                                                 PRIMARY KEY (id),
                                                 KEY meeting_id (meeting_id),
                                                 KEY process_id (process_id),
                                                 CONSTRAINT sm_approve_record_ibfk_1 FOREIGN KEY (meeting_id) REFERENCES sm_meeting_info (id),
                                                 CONSTRAINT sm_approve_record_ibfk_2 FOREIGN KEY (process_id) REFERENCES sm_approve_process_def (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 会议绑定流程定义
ALTER TABLE sm_meeting_info ADD COLUMN process_id bigint DEFAULT NULL AFTER approve_status;

-- 审批任务表（记录每个节点上每个审批人的任务）
CREATE TABLE IF NOT EXISTS sm_approve_task (
                                               id bigint NOT NULL AUTO_INCREMENT,
                                               meeting_id bigint NOT NULL,
                                               node_id varchar(50) NOT NULL,
                                               node_type varchar(20) NOT NULL,
                                               approver_id bigint NOT NULL,
                                               status int DEFAULT 0 COMMENT '0待审批 1通过 2驳回',
                                               action int DEFAULT NULL,
                                               opinion varchar(512) DEFAULT NULL,
                                               approve_time datetime DEFAULT NULL,
                                               PRIMARY KEY (id),
                                               KEY meeting_id (meeting_id),
                                               KEY node_id (node_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS sm_audit_log (
                                            id bigint NOT NULL AUTO_INCREMENT,
                                            operation_type varchar(50) NOT NULL,
                                            target_id bigint DEFAULT NULL,
                                            target_type varchar(50) DEFAULT NULL,
                                            operator_id bigint DEFAULT NULL,
                                            operator_name varchar(50) DEFAULT NULL,
                                            old_value text,
                                            new_value text,
                                            ip_address varchar(50) DEFAULT NULL,
                                            create_time datetime DEFAULT NULL,
                                            PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;