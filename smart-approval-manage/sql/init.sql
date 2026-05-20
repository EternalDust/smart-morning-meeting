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

CREATE TABLE IF NOT EXISTS meeting_agenda (
    id bigint NOT NULL AUTO_INCREMENT,
    meeting_id bigint NOT NULL,
    title varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
    speaker_id bigint DEFAULT NULL,
    duration int DEFAULT NULL,
    sort int DEFAULT NULL,
    create_time datetime DEFAULT NULL,
    PRIMARY KEY (id),
    KEY meeting_id (meeting_id),
    CONSTRAINT meeting_agenda_ibfk_1 FOREIGN KEY (meeting_id) REFERENCES sm_meeting_info (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS meeting_attendee (
    id bigint NOT NULL AUTO_INCREMENT,
    meeting_id bigint NOT NULL,
    user_id bigint NOT NULL,
    role_type int DEFAULT NULL,
    attend_status int DEFAULT NULL,
    invite_time datetime DEFAULT NULL,
    PRIMARY KEY (id),
    KEY meeting_id (meeting_id),
    CONSTRAINT meeting_attendee_ibfk_1 FOREIGN KEY (meeting_id) REFERENCES sm_meeting_info (id)
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
