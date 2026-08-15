-- =====================================================
-- 医疗多源数据采集与治理子系统 - 数据库初始化脚本
-- 库名: smart_meeting, 字符集: utf8mb4
-- 约定: 团队共用 smart_meeting 库, 若库已建则跳过 CREATE DATABASE
-- 注意: data_domain 列为整改新增, 若已由旧版 init-all.sql 建过 data_source_config,
--       请执行下面注释中的 ALTER 补列(或直接以本脚本为准重建):
--       ALTER TABLE data_source_config ADD COLUMN data_domain VARCHAR(20) COMMENT '数据域' AFTER source_type;
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
    data_domain VARCHAR(20)           COMMENT '数据域: HIS/LIS/EMR/PACS/DRUG/MEETING/GENERAL',
    config_json TEXT         NOT NULL COMMENT '连接配置，JSON格式存储',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '采集状态，0=禁用，1=启用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_source_code (source_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源配置表';

-- ---------------------------------------------------
-- 原始数据表（采集落库，数据→可视化链路起点）
-- ---------------------------------------------------
CREATE TABLE IF NOT EXISTS data_raw_data (
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
CREATE TABLE IF NOT EXISTS data_cleaning_rule (
    rule_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    rule_name   VARCHAR(100) NOT NULL COMMENT '规则名称，如"就诊记录去重"',
    rule_type   VARCHAR(20)  NOT NULL COMMENT '规则类型: DEDUP/FORMAT/FILL/VALIDATE',
    rule_config TEXT         NOT NULL COMMENT '规则配置，JSON格式存储',
    priority    INT          NOT NULL DEFAULT 0 COMMENT '数字越小优先级越高',
    enabled     TINYINT      NOT NULL DEFAULT 1 COMMENT '0=禁用，1=启用',
    PRIMARY KEY (rule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='清洗规则表';

-- ---------------------------------------------------
-- 清洗后数据表（清洗链路输出，记录粒度=一次就诊，按 patient_id+visit_time 去重）
-- ---------------------------------------------------
CREATE TABLE IF NOT EXISTS data_clean_data (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    patient_id    VARCHAR(50)   NOT NULL COMMENT '患者ID，业务主键',
    visit_time    DATETIME      NULL     COMMENT '就诊时间（已标准化为 yyyy-MM-dd HH:mm:ss）',
    age           INT           NULL     COMMENT '年龄（缺失填 -1）',
    gender        VARCHAR(10)   NULL     COMMENT '性别（统一为 男/女）',
    diagnosis     VARCHAR(500)  NULL     COMMENT '诊断结果（缺失填"未知"）',
    department    VARCHAR(100)  NULL     COMMENT '科室名称（关联晨会维度）',
    doctor_id     VARCHAR(50)   NULL     COMMENT '医生ID',
    quality_score DECIMAL(5,2)  NOT NULL DEFAULT 0.00 COMMENT '质量评分：完整性40%+一致性30%+有效性30%，>=60 合格',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_patient_id (patient_id),
    KEY idx_visit_time (visit_time),
    KEY idx_quality_score (quality_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='清洗后数据表';

-- ---------------------------------------------------
-- 异常记录表
-- ---------------------------------------------------
CREATE TABLE IF NOT EXISTS data_anomaly_record (
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
CREATE TABLE IF NOT EXISTS data_smart_tag (
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
-- 共享人员表（登录统一账号体系，与团队 sql/init-all.sql 保持一致，幂等建表+种子）
-- 维护者：汪宇涵（本脚本仅保障本子系统单独部署时登录可用）
-- ---------------------------------------------------
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

-- =====================================================
-- 初始化数据（全部幂等：显式主键 + INSERT IGNORE，可重复执行）
-- =====================================================

-- 数据源配置（data_domain 为整改新增的数据域字段）
INSERT IGNORE INTO data_source_config (id, source_code, source_name, source_type, data_domain, config_json, status) VALUES
(1, 'MYSQL_HIS_001', 'HIS门诊业务库', 'mysql', 'HIS',     '{"host":"192.168.1.100","port":3306,"database":"his_db"}', 1),
(2, 'KAFKA_ETL_001', '实时数据管道', 'kafka', 'GENERAL',  '{"bootstrap":"192.168.1.101:9092","topic":"raw-data"}', 1),
(3, 'HTTP_API_001',  '外部数据接口', 'http',  'GENERAL',  '{"url":"https://api.example.com/data","method":"POST"}', 1),
(4, 'MYSQL_PACS_001','PACS影像系统', 'mysql', 'PACS',     '{"host":"192.168.1.102","port":3306,"database":"pacs_db"}', 0);

-- 清洗规则
INSERT IGNORE INTO data_cleaning_rule (rule_id, rule_name, rule_type, rule_config, priority, enabled) VALUES
(1, '就诊记录去重',      'DEDUP',    '{"keyFields":"patient_id,visit_time","strategy":"latest","windowMinutes":30}', 1, 1),
(2, '日期格式标准化',    'FORMAT',   '{"field":"visit_time","sourceFormat":"auto","targetFormat":"yyyy-MM-dd HH:mm:ss"}', 2, 1),
(3, '性别编码标准化',    'FORMAT',   '{"field":"gender","mapping":{"男":"0","女":"1","M":"0","F":"1"}}', 3, 1),
(4, '年龄字段填充',      'FILL',     '{"field":"age","defaultValue":-1,"strategy":"const"}', 4, 1),
(5, '诊断字段填充',      'FILL',     '{"field":"diagnosis","defaultValue":"未知","strategy":"const"}', 5, 1),
(6, '关键字段空值校验',  'VALIDATE',  '{"requiredFields":"patient_id,visit_time","action":"filter"}', 6, 1),
(7, 'JSON格式合法性校验', 'VALIDATE',  '{"action":"filter","condition":"isValidJson"}', 7, 1);

-- 异常记录
INSERT IGNORE INTO data_anomaly_record (id, indicator_name, indicator_value, expected_range, anomaly_level, description, status) VALUES
(1, '门诊量', '350', '200~280', 'high', '门诊量较基线偏离超过30%', 0),
(2, '药占比', '52.00%', '≤ 50%', 'medium', '药占比超过50%阈值', 0),
(3, '手术成功率', '89.5%', '≥ 95%', 'high', '手术成功率低于95%标准', 1);

-- 共享人员表种子（15人，与 sql/init-all.sql 一致，密码均 123456）
INSERT IGNORE INTO sm_gm_members (id, user_id, name, role, dept, password) VALUES
(1,  '1001','张建国',2,'外科',   '123456'),
(2,  '1002','李明辉',2,'外科',   '123456'),
(3,  '1003','王芳',  2,'内科',   '123456'),
(4,  '1004','刘晓东',2,'儿科',   '123456'),
(5,  '1005','陈丽华',2,'妇产科', '123456'),
(6,  '1006','周建军',2,'骨科',   '123456'),
(7,  '1007','赵敏',  2,'急诊科', '123456'),
(8,  '1008','孙伟',  2,'影像科', '123456'),
(9,  '1009','吴志强',2,'外科',   '123456'),
(10, '1010','郑雅文',2,'内科',   '123456'),
(11, '1011','黄志远',2,'麻醉科', '123456'),
(12, '1012','马晓燕',2,'护理部', '123456'),
(13, '2001','杨辉',  1,'管理层', '123456'),
(14, '2002','夏善柱',1,'管理层', '123456'),
(15, '2003','刘勇国',1,'管理层', '123456');

-- 原始数据演示种子（供清洗/仪表盘链路演示）
INSERT IGNORE INTO data_raw_data (id, source_code, data_json, collect_time) VALUES
(1, 'MYSQL_HIS_001', '{"sourceCode":"MYSQL_HIS_001","dataDomain":"HIS","patientId":"P20260814001","visitTime":"2026-08-14 09:30:00","gender":"男","age":45,"diagnosis":"急性阑尾炎","department":"外科","doctorId":"1001"}', '2026-08-14 09:30:00'),
(2, 'MYSQL_HIS_001', '{"sourceCode":"MYSQL_HIS_001","dataDomain":"HIS","patientId":"P20260814002","visitTime":"2026-08-14 10:00:00","gender":"女","age":null,"diagnosis":"","department":"内科","doctorId":"1003"}', '2026-08-14 10:00:00'),
(3, 'KAFKA_ETL_001', '{"sourceCode":"KAFKA_ETL_001","dataDomain":"GENERAL","patientId":"P20260813001","visitTime":"2026-08-13 16:45:00","gender":"M","age":67,"diagnosis":"高血压","department":"内科","doctorId":"1010"}', '2026-08-13 16:45:00');
