-- =====================================================
-- 演示模式 - 数据初始化脚本 (H2 兼容)
-- =====================================================

-- 数据源配置表
CREATE TABLE IF NOT EXISTS data_source_config (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    source_code VARCHAR(50)  NOT NULL,
    source_name VARCHAR(100) NOT NULL,
    source_type VARCHAR(20)  NOT NULL,
    data_domain VARCHAR(20)           COMMENT '数据域: HIS/LIS/EMR/PACS/DRUG/MEETING/GENERAL',
    config_json TEXT         NOT NULL,
    status      TINYINT      NOT NULL DEFAULT 1,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- 原始数据表（采集落库，作为演示模式的数据→可视化链路起点）
CREATE TABLE IF NOT EXISTS data_raw_data (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    source_code  VARCHAR(50)           COMMENT '数据源编码',
    data_json    TEXT                  COMMENT '采集的原始数据(JSON)',
    collect_time DATETIME              COMMENT '采集时间',
    kafka_offset BIGINT                COMMENT 'Kafka偏移量(演示模式为空)',
    PRIMARY KEY (id)
);

-- 清洗后数据表（清洗链路输出）
CREATE TABLE IF NOT EXISTS data_clean_data (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    patient_id    VARCHAR(50)           COMMENT '患者ID',
    visit_time    DATETIME              COMMENT '就诊时间',
    age           INT                   COMMENT '年龄',
    gender        VARCHAR(10)           COMMENT '性别',
    diagnosis     VARCHAR(200)          COMMENT '诊断',
    department    VARCHAR(50)           COMMENT '科室',
    doctor_id     VARCHAR(50)           COMMENT '医生ID',
    quality_score DECIMAL(5,2)          COMMENT '数据质量评分',
    create_time   DATETIME              COMMENT '清洗时间',
    PRIMARY KEY (id)
);

-- 共享人员表（登录统一账号体系，与团队 sql/init-all.sql 对齐）
CREATE TABLE IF NOT EXISTS sm_gm_members (
    id       BIGINT      NOT NULL AUTO_INCREMENT,
    user_id  VARCHAR(50) NOT NULL,
    name     VARCHAR(32)          COMMENT '姓名',
    role     INT                  COMMENT '角色: 1管理层 2科室人员',
    dept     VARCHAR(64)          COMMENT '科室',
    password VARCHAR(64) DEFAULT '123456' COMMENT '密码',
    status   INT         DEFAULT 1 COMMENT '状态: 1启用 0禁用',
    PRIMARY KEY (id)
);

-- 清洗规则表
CREATE TABLE IF NOT EXISTS data_cleaning_rule (
    rule_id     BIGINT       NOT NULL AUTO_INCREMENT,
    rule_name   VARCHAR(100) NOT NULL,
    rule_type   VARCHAR(20)  NOT NULL,
    rule_config TEXT         NOT NULL,
    priority    INT          NOT NULL DEFAULT 0,
    enabled     TINYINT      NOT NULL DEFAULT 1,
    PRIMARY KEY (rule_id)
);

-- 异常记录表
CREATE TABLE IF NOT EXISTS data_anomaly_record (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    indicator_name  VARCHAR(100) NOT NULL,
    indicator_value VARCHAR(100) NOT NULL,
    expected_range  VARCHAR(200),
    anomaly_level   VARCHAR(20)  NOT NULL DEFAULT 'medium',
    description     VARCHAR(500),
    status          TINYINT      NOT NULL DEFAULT 0,
    detect_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- 智能标签表
CREATE TABLE IF NOT EXISTS data_smart_tag (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    entity_type VARCHAR(50)  NOT NULL,
    entity_id   VARCHAR(100) NOT NULL,
    tag_name    VARCHAR(100) NOT NULL,
    tag_level   VARCHAR(20)  NOT NULL DEFAULT 'medium',
    tag_rule    VARCHAR(500),
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- ========== 演示数据 ==========

-- 数据源演示数据（data_domain 为新增的数据域字段）
INSERT INTO data_source_config (source_code, source_name, source_type, data_domain, config_json, status) VALUES
('MYSQL_HIS_001', 'HIS门诊业务库', 'mysql', 'HIS', '{"host":"192.168.1.100","port":3306,"database":"his_db"}', 1),
('KAFKA_ETL_001', '实时数据管道', 'kafka', 'GENERAL', '{"bootstrap":"192.168.1.101:9092","topic":"raw-data"}', 1),
('HTTP_API_001', '外部数据接口', 'http', 'GENERAL', '{"url":"https://api.example.com/data","method":"POST"}', 1),
('MYSQL_PACS_001', 'PACS影像系统', 'mysql', 'PACS', '{"host":"192.168.1.102","port":3306,"database":"pacs_db"}', 0);

-- 清洗规则演示数据
INSERT INTO data_cleaning_rule (rule_name, rule_type, rule_config, priority, enabled) VALUES
('就诊记录去重',      'DEDUP',    '{"keyFields":"patient_id,visit_time","strategy":"latest"}', 1, 1),
('日期格式标准化',    'FORMAT',   '{"field":"visit_time","targetFormat":"yyyy-MM-dd HH:mm:ss"}', 2, 1),
('性别编码标准化',    'FORMAT',   '{"field":"gender","mapping":{"男":"0","女":"1"}}', 3, 1),
('年龄字段填充',      'FILL',     '{"field":"age","defaultValue":-1}', 4, 1),
('关键字段空值校验',  'VALIDATE', '{"requiredFields":"patient_id,visit_time"}', 5, 1);

-- 异常记录演示数据
INSERT INTO data_anomaly_record (indicator_name, indicator_value, expected_range, anomaly_level, description, status) VALUES
('门诊量', '350', '200~280', 'high', '门诊量较基线偏离超过30%', 0),
('药占比', '52.00%', '≤ 50%', 'medium', '药占比超过50%阈值', 0),
('手术成功率', '89.5%', '≥ 95%', 'high', '手术成功率低于95%标准', 1);

-- ========== 共享人员表种子数据（15人，与 sql/init-all.sql 一致） ==========
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

-- ========== 原始数据演示数据（供清洗/仪表盘链路演示） ==========
INSERT INTO data_raw_data (source_code, data_json, collect_time) VALUES
('MYSQL_HIS_001', '{"sourceCode":"MYSQL_HIS_001","dataDomain":"HIS","patientId":"P20260814001","visitTime":"2026-08-14 09:30:00","gender":"男","age":45,"diagnosis":"急性阑尾炎","department":"外科","doctorId":"1001"}', CURRENT_TIMESTAMP),
('MYSQL_HIS_001', '{"sourceCode":"MYSQL_HIS_001","dataDomain":"HIS","patientId":"P20260814002","visitTime":"2026-08-14 10:00:00","gender":"女","age":null,"diagnosis":"","department":"内科","doctorId":"1003"}', CURRENT_TIMESTAMP),
('KAFKA_ETL_001', '{"sourceCode":"KAFKA_ETL_001","dataDomain":"GENERAL","patientId":"P20260813001","visitTime":"2026-08-13 16:45:00","gender":"M","age":67,"diagnosis":"高血压","department":"内科","doctorId":"1010"}', CURRENT_TIMESTAMP);
