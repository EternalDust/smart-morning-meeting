-- ============================================================
-- 智能晨会平台 - 全量数据库初始化脚本（合并5个子系统）
-- 数据库: smart_meeting, 字符集: utf8mb4
-- 子系统:
--   1) smart-report-interaction   晨会报告与交互
--   2) smart-approval-manage      晨会审批与议程管理
--   3) smart-data-collection      医疗多源数据采集与治理
--   4) smart-visual-data          可视化大屏与BI统计
--   5) smart-meeting-supervise    晨会问题督办与闭环管理
-- ============================================================

CREATE DATABASE IF NOT EXISTS smart_meeting DEFAULT CHARSET utf8mb4;
USE smart_meeting;

-- ============================================================
-- 1. 晨会信息主表 (合并: report-interaction + approval-manage)
-- ============================================================
CREATE TABLE IF NOT EXISTS sm_meeting_info (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    title           VARCHAR(255) NOT NULL            COMMENT '会议标题',
    meeting_type    INT          DEFAULT NULL        COMMENT '会议类型',
    dept_id         BIGINT       DEFAULT NULL        COMMENT '科室ID',
    host_id         BIGINT       DEFAULT NULL        COMMENT '主持人ID',
    start_time      DATETIME     DEFAULT NULL        COMMENT '开始时间',
    end_time        DATETIME     DEFAULT NULL        COMMENT '结束时间',
    location        VARCHAR(255) DEFAULT NULL        COMMENT '会议地点',
    status          INT          DEFAULT 0           COMMENT '会议状态: 0未开始 1进行中 2已结束',
    approve_status  INT          DEFAULT NULL        COMMENT '审批状态',
    creator_id      BIGINT       DEFAULT NULL        COMMENT '创建人ID',
    create_time     DATETIME     DEFAULT NULL        COMMENT '创建时间',
    update_time     DATETIME     DEFAULT NULL        COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='晨会信息主表';

-- ============================================================
-- 2. 平台人员表 (report-interaction + password列)
-- ============================================================
CREATE TABLE IF NOT EXISTS sm_gm_members (
    id       BIGINT      NOT NULL AUTO_INCREMENT,
    user_id  VARCHAR(50) NOT NULL COMMENT '用户账号',
    name     VARCHAR(32) DEFAULT NULL COMMENT '姓名',
    role     INT         DEFAULT NULL COMMENT '角色: 1管理层 2科室人员',
    dept     VARCHAR(64) DEFAULT NULL COMMENT '科室',
    password VARCHAR(64) DEFAULT '123456' COMMENT '密码',
    status   INT         DEFAULT 1     COMMENT '状态: 1启用 0禁用',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台人员表';

-- ============================================================
-- 3. 议程表 (approval-manage)
-- ============================================================
CREATE TABLE IF NOT EXISTS sm_meeting_agenda (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    meeting_id  BIGINT       NOT NULL                COMMENT '关联会议ID',
    title       VARCHAR(255) NOT NULL                COMMENT '议程标题',
    speaker_id  BIGINT       DEFAULT NULL            COMMENT '发言人ID',
    duration    INT          DEFAULT NULL            COMMENT '时长(分钟)',
    sort        INT          DEFAULT NULL            COMMENT '排序号',
    create_time DATETIME     DEFAULT NULL            COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_meeting_id (meeting_id),
    CONSTRAINT fk_agenda_meeting FOREIGN KEY (meeting_id) REFERENCES sm_meeting_info (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='晨会议程表';

-- ============================================================
-- 4. 参会人员表 (合并: meeting_attendee + sm_meeting_attendee)
-- ============================================================
CREATE TABLE IF NOT EXISTS sm_meeting_attendee (
    id            BIGINT   NOT NULL AUTO_INCREMENT,
    meeting_id    BIGINT   NOT NULL                COMMENT '关联会议ID',
    user_id       BIGINT   NOT NULL                COMMENT '用户ID',
    role_type     INT      DEFAULT 3               COMMENT '参会角色: 1主持 2汇报 3列席',
    attend_status INT      DEFAULT 0               COMMENT '参会状态: 0已邀请 1已确认',
    invite_time   DATETIME DEFAULT NULL            COMMENT '邀请时间',
    PRIMARY KEY (id),
    KEY idx_meeting_id (meeting_id),
    CONSTRAINT fk_attendee_meeting FOREIGN KEY (meeting_id) REFERENCES sm_meeting_info (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参会人员表';

-- ============================================================
-- 5. 会议材料表 (approval-manage)
-- ============================================================
CREATE TABLE IF NOT EXISTS sm_meeting_material (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    meeting_id    BIGINT       NOT NULL                COMMENT '关联会议ID',
    material_name VARCHAR(255) NOT NULL                COMMENT '材料名称',
    file_url      VARCHAR(512) DEFAULT NULL            COMMENT '文件地址',
    file_size     BIGINT       DEFAULT NULL            COMMENT '文件大小',
    check_status  INT          DEFAULT NULL            COMMENT '审核状态',
    check_opinion VARCHAR(512) DEFAULT NULL            COMMENT '审核意见',
    uploader_id   BIGINT       DEFAULT NULL            COMMENT '上传人ID',
    create_time   DATETIME     DEFAULT NULL            COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_meeting_id (meeting_id),
    CONSTRAINT fk_material_meeting FOREIGN KEY (meeting_id) REFERENCES sm_meeting_info (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会议材料表';

-- ============================================================
-- 6. 晨会签到表 (report-interaction)
-- ============================================================
CREATE TABLE IF NOT EXISTS sm_meeting_signin (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    meeting_id  BIGINT      NOT NULL                COMMENT '关联会议ID',
    user_id     VARCHAR(50) NOT NULL                COMMENT '签到用户ID',
    sign_time   VARCHAR(20) DEFAULT NULL            COMMENT '签到时间',
    sign_type   INT         DEFAULT 2               COMMENT '签到方式',
    sign_status INT         DEFAULT 0               COMMENT '签到状态: 0正常 1迟到',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='晨会签到表';

-- ============================================================
-- 7. 晨会发言转写表 (report-interaction)
-- ============================================================
CREATE TABLE IF NOT EXISTS sm_meeting_speech (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    meeting_id  BIGINT      NOT NULL                COMMENT '关联会议ID',
    speaker_id  VARCHAR(50) NOT NULL                COMMENT '发言人ID',
    content     LONGTEXT    DEFAULT NULL            COMMENT '发言内容',
    speech_time VARCHAR(20) DEFAULT NULL            COMMENT '发言时间',
    key_points  TEXT        DEFAULT NULL            COMMENT '关键点摘要',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='晨会发言转写表';

-- ============================================================
-- 8. 晨会交互表 (report-interaction)
--   interact_type: 1=提问 2=反馈 3=投票
-- ============================================================
CREATE TABLE IF NOT EXISTS sm_meeting_interaction (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    meeting_id    BIGINT      NOT NULL                COMMENT '关联会议ID',
    user_id       VARCHAR(50) NOT NULL                COMMENT '用户ID',
    interact_type INT         DEFAULT 1               COMMENT '交互类型: 1提问 2反馈 3投票',
    content       TEXT        DEFAULT NULL            COMMENT '交互内容',
    reply         TEXT        DEFAULT NULL            COMMENT '回复内容',
    create_time   VARCHAR(20) DEFAULT NULL            COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='晨会交互表';

-- ============================================================
-- 9. 晨会摘要表 (report-interaction)
-- ============================================================
CREATE TABLE IF NOT EXISTS sm_meeting_summary (
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    meeting_id  BIGINT   NOT NULL                    COMMENT '关联会议ID',
    summary     LONGTEXT DEFAULT NULL                COMMENT '会议摘要',
    create_time VARCHAR(20) DEFAULT NULL             COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='晨会摘要表';

-- ============================================================
-- 10. 议程模板表 (approval-manage)
-- ============================================================
CREATE TABLE IF NOT EXISTS sm_meeting_agenda_template (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    template_name VARCHAR(255) NOT NULL                COMMENT '模板名称',
    dept_id       BIGINT       DEFAULT NULL            COMMENT '科室ID',
    content       TEXT         DEFAULT NULL            COMMENT '模板内容',
    creator_id    BIGINT       DEFAULT NULL            COMMENT '创建人ID',
    status        INT          DEFAULT NULL            COMMENT '状态',
    create_time   DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_time   DATETIME     DEFAULT NULL            COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='议程模板表';

-- ============================================================
-- 11. 审批流程定义表 (approval-manage)
-- ============================================================
CREATE TABLE IF NOT EXISTS sm_approve_process_def (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    process_name VARCHAR(255) NOT NULL                COMMENT '流程名称',
    dept_id      BIGINT       DEFAULT NULL            COMMENT '科室ID',
    nodes_json   TEXT         DEFAULT NULL            COMMENT '审批节点(JSON)',
    status       INT          DEFAULT NULL            COMMENT '状态',
    create_time  DATETIME     DEFAULT NULL            COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批流程定义表';

-- ============================================================
-- 12. 审批记录表 (approval-manage)
-- ============================================================
CREATE TABLE IF NOT EXISTS sm_approve_record (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    meeting_id   BIGINT       NOT NULL                COMMENT '关联会议ID',
    process_id   BIGINT       DEFAULT NULL            COMMENT '关联流程ID',
    node_name    VARCHAR(255) DEFAULT NULL            COMMENT '节点名称',
    approver_id  BIGINT       DEFAULT NULL            COMMENT '审批人ID',
    action       INT          DEFAULT NULL            COMMENT '审批动作',
    opinion      VARCHAR(512) DEFAULT NULL            COMMENT '审批意见',
    approve_time DATETIME     DEFAULT NULL            COMMENT '审批时间',
    PRIMARY KEY (id),
    KEY idx_meeting_id (meeting_id),
    KEY idx_process_id (process_id),
    CONSTRAINT fk_approve_meeting FOREIGN KEY (meeting_id) REFERENCES sm_meeting_info (id),
    CONSTRAINT fk_approve_process FOREIGN KEY (process_id) REFERENCES sm_approve_process_def (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批记录表';

-- ============================================================
-- 13. 数据源配置表 (data-collection)
-- ============================================================
CREATE TABLE IF NOT EXISTS data_source_config (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    source_code VARCHAR(50)  NOT NULL                COMMENT '数据源编号',
    source_name VARCHAR(100) NOT NULL                COMMENT '数据源名称',
    source_type VARCHAR(20)  NOT NULL                COMMENT '数据源类型: mysql/kafka/http',
    config_json TEXT         NOT NULL                COMMENT '连接配置(JSON)',
    status      TINYINT      NOT NULL DEFAULT 1      COMMENT '0禁用 1启用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_source_code (source_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源配置表';

-- ============================================================
-- 14. 原始数据表 (data-collection)
-- ============================================================
CREATE TABLE IF NOT EXISTS data_raw_data (
    id           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    source_code  VARCHAR(50)  NOT NULL             COMMENT '关联数据源',
    data_json    LONGTEXT     NOT NULL             COMMENT '原始数据(JSON)',
    collect_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '采集时间',
    kafka_offset BIGINT       NULL                COMMENT 'Kafka偏移量',
    PRIMARY KEY (id),
    KEY idx_source_code (source_code),
    KEY idx_collect_time (collect_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='原始数据表';

-- ============================================================
-- 15. 清洗规则表 (data-collection)
-- ============================================================
CREATE TABLE IF NOT EXISTS data_cleaning_rule (
    rule_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    rule_name   VARCHAR(100) NOT NULL                COMMENT '规则名称',
    rule_type   VARCHAR(20)  NOT NULL                COMMENT '规则类型: DEDUP/FORMAT/FILL/VALIDATE',
    rule_config TEXT         NOT NULL                COMMENT '规则配置(JSON)',
    priority    INT          NOT NULL DEFAULT 0      COMMENT '优先级(越小越高)',
    enabled     TINYINT      NOT NULL DEFAULT 1      COMMENT '0禁用 1启用',
    PRIMARY KEY (rule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='清洗规则表';

-- ============================================================
-- 16. 清洗后数据表 (data-collection)
-- ============================================================
CREATE TABLE IF NOT EXISTS data_clean_data (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    patient_id    VARCHAR(50)   NOT NULL                COMMENT '患者ID',
    visit_time    DATETIME      NULL                    COMMENT '就诊时间',
    age           INT           NULL                    COMMENT '年龄',
    gender        VARCHAR(10)   NULL                    COMMENT '性别',
    diagnosis     VARCHAR(500)  NULL                    COMMENT '诊断结果',
    department    VARCHAR(100)  NULL                    COMMENT '科室',
    doctor_id     VARCHAR(50)   NULL                    COMMENT '医生ID',
    quality_score DECIMAL(5,2)  NOT NULL DEFAULT 0.00   COMMENT '质量评分(0-100)',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_patient_id (patient_id),
    KEY idx_visit_time (visit_time),
    KEY idx_quality_score (quality_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='清洗后数据表';

-- ============================================================
-- 17. 异常记录表 (data-collection)
-- ============================================================
CREATE TABLE IF NOT EXISTS data_anomaly_record (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    indicator_name  VARCHAR(100) NOT NULL                COMMENT '指标名称',
    indicator_value VARCHAR(100) NOT NULL                COMMENT '指标值',
    expected_range  VARCHAR(200) NULL                    COMMENT '正常范围',
    anomaly_level   VARCHAR(20)  NOT NULL DEFAULT 'medium' COMMENT '异常等级: high/medium/low',
    description     VARCHAR(500) NULL                    COMMENT '异常描述',
    status          TINYINT      NOT NULL DEFAULT 0      COMMENT '0未处理 1已处理',
    detect_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '检测时间',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_anomaly_level (anomaly_level),
    KEY idx_status (status),
    KEY idx_detect_time (detect_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异常记录表';

-- ============================================================
-- 18. 智能标签表 (data-collection)
-- ============================================================
CREATE TABLE IF NOT EXISTS data_smart_tag (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    entity_type VARCHAR(50)  NOT NULL                COMMENT '实体类型: doctor/department/patient',
    entity_id   VARCHAR(100) NOT NULL                COMMENT '实体ID',
    tag_name    VARCHAR(100) NOT NULL                COMMENT '标签名称',
    tag_level   VARCHAR(20)  NOT NULL DEFAULT 'medium' COMMENT '标签等级: high/medium/low',
    tag_rule    VARCHAR(500) NULL                    COMMENT '标签生成规则',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_entity_type (entity_type),
    KEY idx_entity_id (entity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能标签表';

-- ============================================================
-- 19. 数据字典与指标维度表 (visual-data)
-- ============================================================
CREATE TABLE IF NOT EXISTS bi_dim_dict (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '字典ID',
    dict_code   VARCHAR(64)  NOT NULL                COMMENT '字典编码',
    dict_name   VARCHAR(255) NOT NULL                COMMENT '字典名称',
    dict_type   VARCHAR(32)  DEFAULT NULL            COMMENT '字典类型',
    sort        INT          DEFAULT NULL            COMMENT '排序号',
    status      INT          DEFAULT NULL            COMMENT '状态',
    create_time VARCHAR(20)  DEFAULT NULL            COMMENT '创建时间',
    update_time VARCHAR(20)  DEFAULT NULL            COMMENT '更新时间',
    profile     VARCHAR(1000) DEFAULT NULL           COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典与指标维度表';

-- ============================================================
-- 20. 晨会统计指标事实表 (visual-data)
-- ============================================================
CREATE TABLE IF NOT EXISTS bi_stat_meeting (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '统计ID',
    stat_date    VARCHAR(255) NOT NULL                COMMENT '统计日期',
    dept_id      BIGINT       DEFAULT NULL            COMMENT '科室ID',
    dept_name    VARCHAR(255) DEFAULT NULL            COMMENT '科室名称',
    meeting_count INT         DEFAULT NULL            COMMENT '晨会场次',
    should_num   INT          DEFAULT NULL            COMMENT '应到人数',
    real_num     INT          DEFAULT NULL            COMMENT '实到人数',
    attend_rate  DECIMAL(5,2) DEFAULT NULL            COMMENT '参会率(%)',
    late_num     INT          DEFAULT NULL            COMMENT '迟到人数',
    absent_num   INT          DEFAULT NULL            COMMENT '缺席人数',
    avg_duration INT          DEFAULT NULL            COMMENT '平均时长(分钟)',
    create_time  VARCHAR(20)  DEFAULT NULL            COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='晨会统计指标事实表';

-- ============================================================
-- 21. 问题督办统计事实表 (visual-data)
-- ============================================================
CREATE TABLE IF NOT EXISTS bi_stat_supervise (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '统计ID',
    stat_date       VARCHAR(255) NOT NULL                COMMENT '统计日期',
    dept_id         BIGINT       DEFAULT NULL            COMMENT '科室ID',
    total_count     INT          DEFAULT NULL            COMMENT '问题总数',
    solved_count    INT          DEFAULT NULL            COMMENT '已解决数',
    solve_rate      DECIMAL(5,2) DEFAULT NULL            COMMENT '解决率(%)',
    overdue_count   INT          DEFAULT NULL            COMMENT '逾期数',
    urgent_count    INT          DEFAULT NULL            COMMENT '紧急问题数',
    avg_handle_hour INT          DEFAULT NULL            COMMENT '平均处理时长',
    create_time     VARCHAR(20)  DEFAULT NULL            COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问题督办统计事实表';

-- ============================================================
-- 22. 医疗业务指标事实表 (visual-data)
-- ============================================================
CREATE TABLE IF NOT EXISTS bi_stat_medical (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '统计ID',
    stat_date     DATE         NOT NULL                COMMENT '统计日期',
    dept_id       BIGINT       DEFAULT NULL            COMMENT '科室ID',
    index_code    VARCHAR(64)  DEFAULT NULL            COMMENT '指标编码',
    index_name    VARCHAR(255) DEFAULT NULL            COMMENT '指标名称',
    index_value   DECIMAL(18,2) DEFAULT NULL           COMMENT '指标值',
    target_value  DECIMAL(18,2) DEFAULT NULL           COMMENT '目标值',
    complete_rate DECIMAL(5,2)  DEFAULT NULL           COMMENT '完成率(%)',
    create_time   VARCHAR(20)  DEFAULT NULL            COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医疗业务指标事实表';

-- ============================================================
-- 23. 异常检测与预警表 (visual-data)
-- ============================================================
CREATE TABLE IF NOT EXISTS bi_warn_record (
    id             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '预警ID',
    warn_type      VARCHAR(32) DEFAULT NULL            COMMENT '预警类型',
    warn_level     INT         DEFAULT NULL            COMMENT '预警等级',
    dept_id        BIGINT      DEFAULT NULL            COMMENT '科室ID',
    index_code     VARCHAR(64) DEFAULT NULL            COMMENT '异常指标',
    abnormal_value VARCHAR(64) DEFAULT NULL            COMMENT '异常值',
    threshold_value VARCHAR(64) DEFAULT NULL           COMMENT '阈值',
    status         INT         DEFAULT NULL            COMMENT '状态',
    handler_id     BIGINT      DEFAULT NULL            COMMENT '处理人ID',
    handle_time    VARCHAR(20) DEFAULT NULL            COMMENT '处理时间',
    create_time    VARCHAR(20) DEFAULT NULL            COMMENT '生成时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异常检测与预警表';

-- ============================================================
-- 24. 可视化大屏配置表 (visual-data)
-- ============================================================
CREATE TABLE IF NOT EXISTS bi_chart_config (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    chart_code   VARCHAR(64)  NOT NULL                COMMENT '图表编码',
    chart_name   VARCHAR(255) DEFAULT NULL            COMMENT '图表名称',
    chart_type   VARCHAR(32)  DEFAULT NULL            COMMENT '图表类型',
    source_table VARCHAR(64)  DEFAULT NULL            COMMENT '数据源表',
    role_ids     VARCHAR(512) DEFAULT NULL            COMMENT '权限角色',
    enabled      INT          DEFAULT NULL            COMMENT '是否启用',
    sort         INT          DEFAULT NULL            COMMENT '排序号',
    update_time  VARCHAR(20)  DEFAULT NULL            COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='可视化大屏配置表';

-- ============================================================
-- 25. 操作日志表 (visual-data)
-- ============================================================
CREATE TABLE IF NOT EXISTS sm_operation_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    user_id     BIGINT       DEFAULT NULL            COMMENT '用户ID',
    user_name   VARCHAR(64)  DEFAULT NULL            COMMENT '用户名',
    module      VARCHAR(64)  DEFAULT NULL            COMMENT '操作模块',
    content     VARCHAR(512) DEFAULT NULL            COMMENT '操作内容',
    ip          VARCHAR(64)  DEFAULT NULL            COMMENT '操作IP',
    create_time VARCHAR(20)  DEFAULT NULL            COMMENT '操作时间',
    result      INT          DEFAULT NULL            COMMENT '执行结果',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================================================
-- 26. 系统用户表 (visual-data)
-- ============================================================
CREATE TABLE IF NOT EXISTS sys_user (
    id        BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username  VARCHAR(50) NOT NULL                 COMMENT '用户名',
    password  VARCHAR(100) NOT NULL                COMMENT '密码',
    role_id   INT         NOT NULL                 COMMENT '角色: 1高层 2中层 3基层',
    dept_id   BIGINT      DEFAULT NULL             COMMENT '科室ID',
    dept_name VARCHAR(50) DEFAULT NULL             COMMENT '科室名称',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ============================================================
-- 27. 问题信息主表 (supervise)
-- ============================================================
CREATE TABLE IF NOT EXISTS sm_problem (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '问题ID',
    title       VARCHAR(255) NOT NULL                COMMENT '问题标题',
    content     TEXT         DEFAULT NULL            COMMENT '问题描述',
    source_type INT          DEFAULT 2               COMMENT '来源: 1自动采集 2手动录入',
    creator_id  BIGINT       DEFAULT NULL            COMMENT '录入人ID',
    assignee_id BIGINT       DEFAULT NULL            COMMENT '负责人ID',
    category    INT          DEFAULT NULL            COMMENT '分类: 1医疗 2运维 3管理',
    risk_level  INT          DEFAULT NULL            COMMENT '风险等级: 1一般 2重要 3紧急',
    priority    INT          DEFAULT NULL            COMMENT 'AI判定优先级',
    status      INT          DEFAULT 0               COMMENT '状态: 0待分派 1处理中 2待复查 3已结案',
    deadline    DATETIME     DEFAULT NULL            COMMENT '截止时间',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_status (status),
    INDEX idx_assignee (assignee_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问题信息主表';

-- ============================================================
-- 28. 任务分派表 (supervise)
-- ============================================================
CREATE TABLE IF NOT EXISTS sm_assign (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '分派ID',
    problem_id  BIGINT       NOT NULL                COMMENT '问题ID',
    user_id     BIGINT       NOT NULL                COMMENT '负责人ID',
    assign_type INT          DEFAULT 1               COMMENT '分派方式: 1自动 2人工',
    operator_id BIGINT       DEFAULT NULL            COMMENT '操作人ID',
    reason      VARCHAR(255) DEFAULT NULL            COMMENT '改派原因',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_problem (problem_id),
    INDEX idx_user (user_id),
    CONSTRAINT fk_assign_problem FOREIGN KEY (problem_id) REFERENCES sm_problem (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务分派表';

-- ============================================================
-- 29. 进度记录表 (supervise)
-- ============================================================
CREATE TABLE IF NOT EXISTS sm_progress (
    id             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '进度ID',
    problem_id     BIGINT        NOT NULL                COMMENT '问题ID',
    progress       INT           DEFAULT 0               COMMENT '进度百分比(0-100)',
    remark         VARCHAR(1024) DEFAULT NULL            COMMENT '备注',
    attachment_url VARCHAR(512)  DEFAULT NULL            COMMENT '附件地址',
    reporter_id    BIGINT        DEFAULT NULL            COMMENT '上报人ID',
    create_time    DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_problem (problem_id),
    INDEX idx_create_time (create_time),
    CONSTRAINT fk_progress_problem FOREIGN KEY (problem_id) REFERENCES sm_problem (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='进度记录表';

-- ============================================================
-- 30. 督办文书表 (supervise)
-- ============================================================
CREATE TABLE IF NOT EXISTS sm_document (
    id           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '文书ID',
    problem_id   BIGINT   NOT NULL                COMMENT '问题ID',
    doc_type     INT      DEFAULT 1               COMMENT '文书类型: 1督办单 2整改通知书 3闭环报告',
    content      LONGTEXT DEFAULT NULL            COMMENT '文书内容',
    gen_type     INT      DEFAULT 1               COMMENT '生成方式: 1AI生成 2人工编辑',
    check_status INT      DEFAULT 0               COMMENT '审核状态: 0待审核 1通过 2驳回',
    create_time  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_problem (problem_id),
    CONSTRAINT fk_document_problem FOREIGN KEY (problem_id) REFERENCES sm_problem (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='督办文书表';

-- ============================================================
-- 31. 预警配置表 (supervise)
-- ============================================================
CREATE TABLE IF NOT EXISTS sm_alert_config (
    id             BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    threshold_hour INT    DEFAULT 24               COMMENT '预警阈值(小时)',
    channel        INT    DEFAULT 1                COMMENT '提醒渠道: 1站内信 2短信 3两者',
    status         INT    DEFAULT 1                COMMENT '状态: 0禁用 1启用',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警配置表';


-- ============================================================
-- 系统初始化数据（清洗规则、预警配置、系统用户）
-- ============================================================

-- 清洗规则
INSERT INTO data_cleaning_rule (rule_name, rule_type, rule_config, priority, enabled) VALUES
('就诊记录去重',       'DEDUP',    '{"keyFields":"patient_id,visit_time","strategy":"latest","windowMinutes":30}', 1, 1),
('日期格式标准化',     'FORMAT',   '{"field":"visit_time","sourceFormat":"auto","targetFormat":"yyyy-MM-dd HH:mm:ss"}', 2, 1),
('性别编码标准化',     'FORMAT',   '{"field":"gender","mapping":{"男":"0","女":"1","M":"0","F":"1"}}', 3, 1),
('年龄字段填充',       'FILL',     '{"field":"age","defaultValue":-1,"strategy":"const"}', 4, 1),
('诊断字段填充',       'FILL',     '{"field":"diagnosis","defaultValue":"未知","strategy":"const"}', 5, 1),
('关键字段空值校验',   'VALIDATE', '{"requiredFields":"patient_id,visit_time","action":"filter"}', 6, 1),
('JSON格式合法性校验', 'VALIDATE', '{"action":"filter","condition":"isValidJson"}', 7, 1);

-- 预警配置
INSERT INTO sm_alert_config (threshold_hour, channel, status) VALUES
(24, 1, 1),
(48, 2, 1);

-- 系统用户
INSERT INTO sys_user (username, password, role_id, dept_id, dept_name) VALUES
('admin',   'hqh123', 1, 100, '全院汇总'),
('ken101',  'hqh123', 2, 101, '心内科'),
('staff101','hqh123', 3, 101, '心内科');


-- ============================================================
-- 晨会场景演示数据
-- 会议日期: 2026-05-29（周一科室晨会）
-- ============================================================

-- 1. 晨会信息
INSERT INTO sm_meeting_info (id, title, host_id, start_time, end_time, status, create_time) VALUES
(1, '周一科室晨会', 2001, '2026-05-29 08:30:00', '2026-05-29 09:30:00', 1, '2026-05-29 08:00:00');

-- 2. 平台人员（15人）
--    1001~1012: 临床科室人员 role=2;  2001~2003: 管理层 role=1
INSERT INTO sm_gm_members (user_id, name, role, dept, password) VALUES
('1001','张建国',2,'外科',   '123456'),
('1002','李明辉',2,'外科',   '123456'),
('1003','王芳',  2,'内科',   '123456'),
('1004','刘晓东',2,'儿科',   '123456'),
('1005','陈丽华',2,'妇产科', '123456'),
('1006','周建军',2,'骨科',   '123456'),
('1007','赵敏',  2,'急诊科', '123456'),
('1008','孙伟',  2,'影像科', '123456'),
('1009','吴志强',2,'外科',   '123456'),
('1010','郑雅文',2,'内科',   '123456'),
('1011','黄志远',2,'麻醉科', '123456'),
('1012','马晓燕',2,'护理部', '123456'),
('2001','杨辉',  1,'管理层', '123456'),
('2002','夏善柱',1,'管理层', '123456'),
('2003','刘勇国',1,'管理层', '123456');

-- 3. 参会人员（全部15人参会）
INSERT INTO sm_meeting_attendee (meeting_id, user_id, role_type, attend_status) VALUES
(1,1001,2,0),(1,1002,2,0),(1,1003,2,0),(1,1004,2,0),(1,1005,2,0),
(1,1006,2,0),(1,1007,2,0),(1,1008,2,0),(1,1009,2,0),(1,1010,2,0),
(1,1011,2,0),(1,1012,2,0),(1,2001,1,0),(1,2002,1,0),(1,2003,1,0);

-- 4. 签到记录（5人已签到，1003未签到）
INSERT INTO sm_meeting_signin (meeting_id, user_id, sign_time, sign_status) VALUES
(1, '1001', '2026-05-29 08:30:15', 0),   -- 正常签到
(1, '1002', '2026-05-29 08:32:40', 1),   -- 迟到
(1, '1004', '2026-05-29 08:29:50', 0),   -- 正常签到
(1, '1005', '2026-05-29 08:31:05', 0);   -- 正常签到

-- 5. 发言记录
INSERT INTO sm_meeting_speech (meeting_id, speaker_id, content, speech_time) VALUES
(1, '1001', '本月门诊量2350人次，同比增长8.2%，其中专家门诊增长12.5%',   '2026-05-29 08:35:00'),
(1, '1002', '外科手术量42台，其中三级以上手术18台，占比42.9%',          '2026-05-29 08:42:00');

-- 6. 交互记录
INSERT INTO sm_meeting_interaction (meeting_id, user_id, interact_type, content, create_time) VALUES
(1, '2001', 3, '下月是否将晨会时间调整为08:00',              '2026-05-29 08:48:00'),
(1, '1006', 2, '建议按科室拆分门诊数据，便于各科室对照分析',  '2026-05-29 08:55:00'),
(1, '1009', 1, '外科手术量增长的具体原因是什么？',           '2026-05-29 09:02:00');

-- 7. 督办问题
INSERT INTO sm_problem (id, title, content, source_type, creator_id, assignee_id, category, risk_level, status, create_time) VALUES
(1, '内科药品库存不足', '近期门诊量上升，内科常用药品（降压药、降糖药）库存已降至预警线以下，需尽快补充。',
 2, 2001, 1010, 2, 2, 1, '2026-05-29 09:15:00');

-- 8. BI统计
INSERT INTO bi_stat_meeting (stat_date, dept_id, dept_name, meeting_count, should_num, real_num, attend_rate, late_num, absent_num, create_time) VALUES
('2026-05-29', 1, '外科', 1, 15, 5, 33.33, 1, 10, '2026-05-29 09:30:00');
