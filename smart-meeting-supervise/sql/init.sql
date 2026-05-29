-- =====================================================
-- 晨会问题督办与闭环管理子系统 - 数据库初始化脚本
-- 子系统: smart-meeting-supervise
-- 数据库: smart_meeting
-- 字符集: utf8mb4
-- =====================================================

USE smart_meeting;

-- =====================================================
-- 1. 问题信息主表
-- =====================================================
DROP TABLE IF EXISTS sm_problem;
CREATE TABLE sm_problem (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '问题ID',
                            title VARCHAR(255) NOT NULL COMMENT '问题标题',
                            content TEXT COMMENT '问题描述',
                            source_type INT DEFAULT 2 COMMENT '来源类型: 1自动采集 2手动录入',
                            creator_id BIGINT COMMENT '录入人ID',
                            assignee_id BIGINT COMMENT '负责人ID',
                            category INT COMMENT '问题分类: 1医疗 2运维 3管理',
                            risk_level INT COMMENT '风险等级: 1一般 2重要 3紧急',
                            priority INT COMMENT '优先级(AI判定)',
                            status INT DEFAULT 0 COMMENT '当前状态: 0待分派 1处理中 2待复查 3已结案',
                            deadline DATETIME COMMENT '截止时间',
                            create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            INDEX idx_status (status),
                            INDEX idx_assignee (assignee_id),
                            INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问题信息主表';

-- =====================================================
-- 2. 任务分派表
-- =====================================================
DROP TABLE IF EXISTS sm_assign;
CREATE TABLE sm_assign (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分派ID',
                           problem_id BIGINT NOT NULL COMMENT '问题ID',
                           user_id BIGINT NOT NULL COMMENT '分派对象ID(负责人)',
                           assign_type INT DEFAULT 1 COMMENT '分派方式: 1自动 2人工',
                           operator_id BIGINT COMMENT '操作人ID(人工改派时记录)',
                           reason VARCHAR(255) COMMENT '改派原因',
                           create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           FOREIGN KEY (problem_id) REFERENCES sm_problem(id) ON DELETE CASCADE,
                           INDEX idx_problem (problem_id),
                           INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务分派表';

-- =====================================================
-- 3. 进度记录表
-- =====================================================
DROP TABLE IF EXISTS sm_progress;
CREATE TABLE sm_progress (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
                             problem_id BIGINT NOT NULL COMMENT '问题ID',
                             progress INT DEFAULT 0 COMMENT '进度百分比(0-100)',
                             remark VARCHAR(1024) COMMENT '备注说明',
                             attachment_url VARCHAR(512) COMMENT '附件地址',
                             reporter_id BIGINT COMMENT '上报人ID',
                             create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             FOREIGN KEY (problem_id) REFERENCES sm_problem(id) ON DELETE CASCADE,
                             INDEX idx_problem (problem_id),
                             INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='进度记录表';

-- =====================================================
-- 4. 督办文书表
-- =====================================================
DROP TABLE IF EXISTS sm_document;
CREATE TABLE sm_document (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
                             problem_id BIGINT NOT NULL COMMENT '问题ID',
                             doc_type INT DEFAULT 1 COMMENT '文书类型: 1督办单 2整改通知书 3闭环报告',
                             content LONGTEXT COMMENT '文书内容',
                             gen_type INT DEFAULT 1 COMMENT '生成方式: 1AI生成 2人工编辑',
                             check_status INT DEFAULT 0 COMMENT '审核状态: 0待审核 1通过 2驳回',
                             create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             FOREIGN KEY (problem_id) REFERENCES sm_problem(id) ON DELETE CASCADE,
                             INDEX idx_problem (problem_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='督办文书表';

-- =====================================================
-- 5. 预警配置表
-- =====================================================
DROP TABLE IF EXISTS sm_alert_config;
CREATE TABLE sm_alert_config (
                                 id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
                                 threshold_hour INT DEFAULT 24 COMMENT '预警阈值小时数',
                                 channel INT DEFAULT 1 COMMENT '提醒渠道: 1站内信 2短信 3两者',
                                 status INT DEFAULT 1 COMMENT '状态: 0禁用 1启用'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警配置表';

-- =====================================================
-- 6. 初始化数据（预警配置）
-- =====================================================
INSERT INTO sm_alert_config (threshold_hour, channel, status) VALUES
                                                                  (24, 1, 1),
                                                                  (48, 2, 1);

-- =====================================================
-- 注意：用户表使用共享表 sm_gm_members，不自建！
-- sm_gm_members 由汪宇涵维护，包含字段：id, account, name, role, dept, phone, status
-- =====================================================

-- =====================================================
-- 查询验证
-- =====================================================
SHOW TABLES LIKE 'sm_%';