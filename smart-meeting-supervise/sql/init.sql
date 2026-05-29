-- =====================================================
-- 晨会问题督办与闭环管理子系统 - 数据库初始化脚本
-- 子系统: smart-meeting-supervise
-- 数据库: smart_meeting
-- 字符集: utf8mb4
-- =====================================================

-- 使用数据库（如果已存在则跳过，非第一个建库的人注释掉下面两行）
--CREATE DATABASE IF NOT EXISTS smart_meeting
--DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE smart_meeting;

-- =====================================================
-- 1. 问题信息主表 (对应报告表3-7)
-- =====================================================
DROP TABLE IF EXISTS sm_supervise_problem;
CREATE TABLE sm_supervise_problem (
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
-- 2. 任务分派表 (对应报告表3-8)
-- =====================================================
DROP TABLE IF EXISTS sm_supervise_assign;
CREATE TABLE sm_supervise_assign (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分派ID',
                                     problem_id BIGINT NOT NULL COMMENT '问题ID',
                                     user_id BIGINT NOT NULL COMMENT '分派对象ID(负责人)',
                                     assign_type INT DEFAULT 1 COMMENT '分派方式: 1自动 2人工',
                                     operator_id BIGINT COMMENT '操作人ID(人工改派时记录)',
                                     reason VARCHAR(255) COMMENT '改派原因',
                                     create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     FOREIGN KEY (problem_id) REFERENCES sm_supervise_problem(id) ON DELETE CASCADE,
                                     INDEX idx_problem (problem_id),
                                     INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务分派表';

-- =====================================================
-- 3. 进度记录表 (对应报告表3-9)
-- =====================================================
DROP TABLE IF EXISTS sm_supervise_progress;
CREATE TABLE sm_supervise_progress (
                                       id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
                                       problem_id BIGINT NOT NULL COMMENT '问题ID',
                                       progress INT DEFAULT 0 COMMENT '进度百分比(0-100)',
                                       remark VARCHAR(1024) COMMENT '备注说明',
                                       attachment_url VARCHAR(512) COMMENT '附件地址',
                                       reporter_id BIGINT COMMENT '上报人ID',
                                       create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       FOREIGN KEY (problem_id) REFERENCES sm_supervise_problem(id) ON DELETE CASCADE,
                                       INDEX idx_problem (problem_id),
                                       INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='进度记录表';

-- =====================================================
-- 4. 督办文书表 (对应报告表3-11)
-- =====================================================
DROP TABLE IF EXISTS sm_supervise_document;
CREATE TABLE sm_supervise_document (
                                       id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
                                       problem_id BIGINT NOT NULL COMMENT '问题ID',
                                       doc_type INT DEFAULT 1 COMMENT '文书类型: 1督办单 2整改通知书 3闭环报告',
                                       content LONGTEXT COMMENT '文书内容',
                                       gen_type INT DEFAULT 1 COMMENT '生成方式: 1AI生成 2人工编辑',
                                       check_status INT DEFAULT 0 COMMENT '审核状态: 0待审核 1通过 2驳回',
                                       create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       FOREIGN KEY (problem_id) REFERENCES sm_supervise_problem(id) ON DELETE CASCADE,
                                       INDEX idx_problem (problem_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='督办文书表';

-- =====================================================
-- 5. 预警配置表 (对应报告表3-10)
-- =====================================================
DROP TABLE IF EXISTS sm_supervise_alert_config;
CREATE TABLE sm_supervise_alert_config (
                                           id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
                                           threshold_hour INT DEFAULT 24 COMMENT '预警阈值小时数(提前多少小时预警)',
                                           channel INT DEFAULT 1 COMMENT '提醒渠道: 1站内信 2短信 3两者',
                                           status INT DEFAULT 1 COMMENT '状态: 0禁用 1启用'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警配置表';

-- =====================================================
-- 6. 用户信息表 (对应报告表3-12)
-- =====================================================
DROP TABLE IF EXISTS sm_supervise_user;
CREATE TABLE sm_supervise_user (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
                                   account VARCHAR(64) NOT NULL UNIQUE COMMENT '账号',
                                   password VARCHAR(128) NOT NULL COMMENT '密码',
                                   name VARCHAR(32) COMMENT '姓名',
                                   role INT DEFAULT 2 COMMENT '角色: 1督办专员 2执行责任人 3晨会参会人 4管理员',
                                   dept VARCHAR(64) COMMENT '部门',
                                   phone VARCHAR(16) COMMENT '电话',
                                   status INT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
                                   create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   INDEX idx_account (account),
                                   INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- =====================================================
-- 初始化数据
-- =====================================================

-- 插入预警配置默认数据
INSERT INTO sm_supervise_alert_config (threshold_hour, channel, status) VALUES
                                                                            (24, 1, 1),
                                                                            (48, 2, 1);

-- 插入用户测试数据
INSERT INTO sm_supervise_user (account, password, name, role, dept) VALUES
                                                                        ('admin', '123456', '系统管理员', 4, '信息科'),
                                                                        ('supervisor', '123456', '张督办', 1, '质控办'),
                                                                        ('worker1', '123456', '李医生', 2, 'ICU科室'),
                                                                        ('worker2', '123456', '王工程师', 2, '设备科'),
                                                                        ('participant', '123456', '赵护士', 3, '护理部');

-- =====================================================
-- 共享表说明（本子系统使用的共享表）
-- 以下表属于多个子系统共用，改结构前需在群里沟通
-- =====================================================

-- sm_gm_members: 人员信息（工号、姓名、部门）- 维护者: 汪宇涵
-- sm_meeting_info: 晨会主表 - 维护者: 杨子亨（待建）
-- meeting_attendee: 参会人员名单 - 维护者: 汪宇涵
-- meeting_agenda: 议程表 - 维护者: 杨子亨（待建）
-- sm_org: 组织/科室结构 - 维护者: 待定

-- =====================================================
-- 查询验证
-- =====================================================

-- 查看所有表
SHOW TABLES LIKE 'sm_supervise_%';

-- 查看用户数据
SELECT id, account, name, role, dept FROM sm_supervise_user;
