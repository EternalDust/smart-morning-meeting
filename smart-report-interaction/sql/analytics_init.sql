-- 大数据分析结果表
-- 先执行建表，再跑 Spark 作业灌数据，后端 API 直接查询

CREATE TABLE IF NOT EXISTS sm_meeting_analytics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id BIGINT NOT NULL,
    meeting_title VARCHAR(200),
    meeting_date VARCHAR(20),
    should_attend INT DEFAULT 0,
    actual_attend INT DEFAULT 0,
    attend_rate DECIMAL(5,2) DEFAULT 0,
    normal_count INT DEFAULT 0,
    late_count INT DEFAULT 0,
    speech_count INT DEFAULT 0,
    interaction_count INT DEFAULT 0,
    quality_score DECIMAL(5,2) DEFAULT 0,
    created_at VARCHAR(20),
    updated_at VARCHAR(20)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sm_department_analytics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    department VARCHAR(100),
    meeting_count INT DEFAULT 0,
    avg_attend_rate DECIMAL(5,2) DEFAULT 0,
    total_speech_count INT DEFAULT 0,
    total_interaction_count INT DEFAULT 0,
    period_start VARCHAR(20),
    period_end VARCHAR(20),
    updated_at VARCHAR(20)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sm_member_analytics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(20) NOT NULL,
    user_name VARCHAR(50),
    department VARCHAR(100),
    total_meetings INT DEFAULT 0,
    attended_count INT DEFAULT 0,
    attend_rate DECIMAL(5,2) DEFAULT 0,
    normal_count INT DEFAULT 0,
    late_count INT DEFAULT 0,
    speech_count INT DEFAULT 0,
    interaction_count INT DEFAULT 0,
    last_updated VARCHAR(20)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
