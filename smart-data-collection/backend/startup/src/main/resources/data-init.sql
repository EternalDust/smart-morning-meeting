-- =====================================================
-- 演示模式 - 数据初始化脚本 (H2 兼容)
-- =====================================================

-- 数据源配置表
CREATE TABLE IF NOT EXISTS data_source_config (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    source_code VARCHAR(50)  NOT NULL,
    source_name VARCHAR(100) NOT NULL,
    source_type VARCHAR(20)  NOT NULL,
    config_json TEXT         NOT NULL,
    status      TINYINT      NOT NULL DEFAULT 1,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
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

-- 数据源演示数据
INSERT INTO data_source_config (source_code, source_name, source_type, config_json, status) VALUES
('MYSQL_HIS_001', 'HIS门诊业务库', 'mysql', '{"host":"192.168.1.100","port":3306,"database":"his_db"}', 1),
('KAFKA_ETL_001', '实时数据管道', 'kafka', '{"bootstrap":"192.168.1.101:9092","topic":"raw-data"}', 1),
('HTTP_API_001', '外部数据接口', 'http', '{"url":"https://api.example.com/data","method":"POST"}', 1),
('MYSQL_PACS_001', 'PACS影像系统', 'mysql', '{"host":"192.168.1.102","port":3306,"database":"pacs_db"}', 0);

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
