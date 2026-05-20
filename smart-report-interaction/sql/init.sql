CREATE DATABASE IF NOT EXISTS smart_meeting DEFAULT CHARSET utf8mb4;
USE smart_meeting;

-- 1. 晨会信息主表
CREATE TABLE sm_meeting_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    host_id VARCHAR(50),
    start_time VARCHAR(20),
    end_time VARCHAR(20),
    status INT DEFAULT 0,
    create_time VARCHAR(20)
);

-- 2. 晨会签到表
CREATE TABLE sm_meeting_signin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    meeting_id BIGINT NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    sign_time VARCHAR(20),
    sign_type INT DEFAULT 2,
    sign_status INT DEFAULT 0
);

-- 3. 晨会发言转写表
CREATE TABLE sm_meeting_speech (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    meeting_id BIGINT NOT NULL,
    speaker_id VARCHAR(50) NOT NULL,
    content LONGTEXT,
    speech_time VARCHAR(20),
    key_points TEXT
);

-- 4. 晨会交互表
CREATE TABLE sm_meeting_interaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    meeting_id BIGINT NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    interact_type INT DEFAULT 1,
    content TEXT,
    reply TEXT,
    create_time VARCHAR(20)
);

-- 5. 晨会摘要表
CREATE TABLE sm_meeting_summary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    meeting_id BIGINT NOT NULL,
    summary LONGTEXT,
    create_time VARCHAR(20)
);

-- 6. 平台人员表
CREATE TABLE sm_gm_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(32),
    role INT,
    dept VARCHAR(64),
    status INT DEFAULT 1
);

-- 7. 参会人员表
CREATE TABLE meeting_attendee (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    meeting_id BIGINT NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    role_type INT DEFAULT 3,
    attend_status INT DEFAULT 0
);

-- 测试数据
INSERT INTO sm_meeting_info (id, title, host_id, start_time, end_time, status) VALUES
(1, '周一科室晨会', '2001', '08:30', '09:30', 1);

INSERT INTO sm_gm_members (user_id, name, role, dept) VALUES
('1001','张建国',2,'外科'),('1002','李明辉',2,'外科'),('1003','王芳',2,'内科'),
('1004','刘晓东',2,'儿科'),('1005','陈丽华',2,'妇产科'),('1006','周建军',2,'骨科'),
('1007','赵敏',2,'急诊科'),('1008','孙伟',2,'影像科'),('1009','吴志强',2,'外科'),
('1010','郑雅文',2,'内科'),('1011','黄志远',2,'麻醉科'),('1012','马晓燕',2,'护理部'),
('2001','杨辉',1,'管理层'),('2002','夏善柱',1,'管理层'),('2003','刘勇国',1,'管理层');

INSERT INTO meeting_attendee (meeting_id, user_id, role_type, attend_status) VALUES
(1,'1001',2,0),(1,'1002',2,0),(1,'1003',2,0),(1,'1004',2,0),(1,'1005',2,0),
(1,'1006',2,0),(1,'1007',2,0),(1,'1008',2,0),(1,'1009',2,0),(1,'1010',2,0),
(1,'1011',2,0),(1,'1012',2,0),(1,'2001',1,0),(1,'2002',1,0),(1,'2003',1,0);
