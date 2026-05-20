-- =====================================================
-- 医疗多源数据采集与治理子系统 - 数据库初始化脚本
-- 库名: smart_meeting, 字符集: utf8mb4
-- =====================================================

CREATE DATABASE IF NOT EXISTS smart_meeting
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE smart_meeting;

-- ---------------------------------------------------
-- 数据源配置表
-- ---------------------------------------------------
CREATE TABLE IF NOT EXISTS data_source_config (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    source_code VARCHAR(50)  NOT NULL COMMENT '数据源编号，唯一标识',
    source_name VARCHAR(100) NOT NULL COMMENT '数据源名称，如HIS门诊业务库',
    source_type VARCHAR(20)  NOT NULL COMMENT '数据源类型: mysql/kafka/http',
    config_json TEXT         NOT NULL COMMENT '连接配置，JSON格式存储',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '采集状态，0=禁用，1=启用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_source_code (source_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源配置表';

-- ---------------------------------------------------
-- 原始数据表
-- ---------------------------------------------------
CREATE TABLE IF NOT EXISTS raw_data (
    id            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    source_code   VARCHAR(50)  NOT NULL COMMENT '关联数据源配置表',
    data_json     LONGTEXT     NOT NULL COMMENT '原始数据内容，JSON格式存储',
    collect_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '采集时间',
    kafka_offset  BIGINT       NULL     COMMENT 'Kafka偏移量，用于数据回溯',
    PRIMARY KEY (id),
    KEY idx_source_code (source_code),
    KEY idx_collect_time (collect_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='原始数据表';

-- ---------------------------------------------------
-- 清洗规则表
-- ---------------------------------------------------
CREATE TABLE IF NOT EXISTS cleaning_rule (
    rule_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    rule_name   VARCHAR(100) NOT NULL COMMENT '规则名称，如"就诊记录去重"',
    rule_type   VARCHAR(20)  NOT NULL COMMENT '规则类型: DEDUP/FORMAT/FILL/VALIDATE',
    rule_config TEXT         NOT NULL COMMENT '规则配置，JSON格式存储',
    priority    INT          NOT NULL DEFAULT 0 COMMENT '数字越小优先级越高',
    enabled     TINYINT      NOT NULL DEFAULT 1 COMMENT '0=禁用，1=启用',
    PRIMARY KEY (rule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='清洗规则表';

-- ---------------------------------------------------
-- 清洗后数据表
-- ---------------------------------------------------
CREATE TABLE IF NOT EXISTS clean_data (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    patient_id    VARCHAR(50)   NOT NULL COMMENT '患者ID，业务主键',
    visit_time    DATETIME      NULL     COMMENT '就诊时间',
    age           INT           NULL     COMMENT '年龄',
    gender        VARCHAR(10)   NULL     COMMENT '性别：0=男，1=女',
    diagnosis     VARCHAR(500)  NULL     COMMENT '诊断结果',
    department    VARCHAR(100)  NULL     COMMENT '科室名称',
    doctor_id     VARCHAR(50)   NULL     COMMENT '医生ID',
    quality_score DECIMAL(5,2)  NOT NULL DEFAULT 0.00 COMMENT '质量评分，0-100分',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_patient_id (patient_id),
    KEY idx_visit_time (visit_time),
    KEY idx_quality_score (quality_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='清洗后数据表';

-- ---------------------------------------------------
-- 异常记录表
-- ---------------------------------------------------
CREATE TABLE IF NOT EXISTS anomaly_record (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    indicator_name  VARCHAR(100) NOT NULL COMMENT '指标名称，如"门诊量"',
    indicator_value VARCHAR(100) NOT NULL COMMENT '指标值',
    expected_range  VARCHAR(200) NULL     COMMENT '正常范围',
    anomaly_level   VARCHAR(20)  NOT NULL DEFAULT 'medium' COMMENT '异常等级: high/medium/low',
    description     VARCHAR(500) NULL     COMMENT '异常描述',
    status          TINYINT      NOT NULL DEFAULT 0 COMMENT '处理状态，0=未处理，1=已处理',
    detect_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '检测时间',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_anomaly_level (anomaly_level),
    KEY idx_status (status),
    KEY idx_detect_time (detect_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异常记录表';

-- ---------------------------------------------------
-- 智能标签表
-- ---------------------------------------------------
CREATE TABLE IF NOT EXISTS smart_tag (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    entity_type VARCHAR(50)  NOT NULL COMMENT '实体类型: doctor/department/patient',
    entity_id   VARCHAR(100) NOT NULL COMMENT '实体ID',
    tag_name    VARCHAR(100) NOT NULL COMMENT '标签名称',
    tag_level   VARCHAR(20)  NOT NULL DEFAULT 'medium' COMMENT '标签等级: high/medium/low',
    tag_rule    VARCHAR(500) NULL     COMMENT '标签生成规则',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_entity_type (entity_type),
    KEY idx_entity_id (entity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能标签表';

-- ---------------------------------------------------
-- 初始化清洗规则数据
-- ---------------------------------------------------
INSERT INTO cleaning_rule (rule_name, rule_type, rule_config, priority, enabled) VALUES
('就诊记录去重',      'DEDUP',    '{"keyFields":"patient_id,visit_time","strategy":"latest","windowMinutes":30}', 1, 1),
('日期格式标准化',    'FORMAT',   '{"field":"visit_time","sourceFormat":"auto","targetFormat":"yyyy-MM-dd HH:mm:ss"}', 2, 1),
('性别编码标准化',    'FORMAT',   '{"field":"gender","mapping":{"男":"0","女":"1","M":"0","F":"1"}}', 3, 1),
('年龄字段填充',      'FILL',     '{"field":"age","defaultValue":-1,"strategy":"const"}', 4, 1),
('诊断字段填充',      'FILL',     '{"field":"diagnosis","defaultValue":"未知","strategy":"const"}', 5, 1),
('关键字段空值校验',  'VALIDATE',  '{"requiredFields":"patient_id,visit_time","action":"filter"}', 6, 1),
('JSON格式合法性校验', 'VALIDATE',  '{"action":"filter","condition":"isValidJson"}', 7, 1);
