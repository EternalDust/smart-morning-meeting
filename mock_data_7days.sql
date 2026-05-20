USE smm_db;

-- 清空存在的测试数据以防止记录重复
TRUNCATE TABLE bi_stat_meeting;
TRUNCATE TABLE bi_stat_supervise;

-- 1. 插入图表左侧：过去 7 天的核心指标/参会率数据 (折线图数据)
-- 日期区间：2026-05-13 倒序至 2026-05-19
INSERT INTO bi_stat_meeting 
  (stat_date, dept_id, dept_name, meeting_count, should_num, real_num, attend_rate, late_num, absent_num, avg_duration, create_time)
VALUES
  ('2026-05-13', 100, '全院汇总', 12, 600, 564, 94.00, 12, 24, 30, '2026-05-13 08:30:00'),
  ('2026-05-14', 100, '全院汇总', 12, 600, 576, 96.00, 8, 16, 28, '2026-05-14 08:30:00'),
  ('2026-05-15', 100, '全院汇总', 12, 600, 540, 90.00, 20, 40, 35, '2026-05-15 08:30:00'),
  ('2026-05-16', 100, '全院汇总', 12, 600, 588, 98.00, 4, 8, 25, '2026-05-16 08:30:00'),
  ('2026-05-17', 100, '全院汇总', 12, 600, 595, 99.16, 2, 3, 26, '2026-05-17 08:30:00'),
  ('2026-05-18', 100, '全院汇总', 12, 600, 550, 91.66, 15, 35, 33, '2026-05-18 08:30:00'),
  ('2026-05-19', 100, '全院汇总', 12, 600, 575, 95.83, 10, 15, 29, '2026-05-19 08:30:00');

-- 2. 插入图表右侧：不同科室/部门的异常问题分布 (饼图数据)
-- 为了图表美观，这里给几个典型的科室写入统计量
INSERT INTO bi_stat_supervise 
  (stat_date, dept_id, total_count, solved_count, solve_rate, overdue_count, urgent_count, avg_handle_hour, create_time)
VALUES
  ('2026-05-19', 101, 105, 95, 90.47, 2, 5, 24, '2026-05-19 09:00:00'),   -- 心内科(在前端显示为科室 101)
  ('2026-05-19', 102, 75, 70, 93.33, 1, 3, 18, '2026-05-19 09:00:00'),    -- 神经内科(科室 102)
  ('2026-05-19', 103, 58, 48, 82.75, 4, 8, 36, '2026-05-19 09:00:00'),    -- 急诊科(科室 103)
  ('2026-05-19', 104, 48, 45, 93.75, 0, 2, 12, '2026-05-19 09:00:00');    -- 骨外科(科室 104)

USE smm_db;

CREATE TABLE IF NOT EXISTS sys_user (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        username VARCHAR(50) NOT NULL UNIQUE,
                                        password VARCHAR(100) NOT NULL,
                                        role_id INT NOT NULL COMMENT '1:高层决策人员, 2:中层管理人员, 3:基层操作人员',
                                        dept_id BIGINT COMMENT '关联科室ID，高层可为空',
                                        dept_name VARCHAR(50) COMMENT '科室名称'
);

TRUNCATE TABLE sys_user;
INSERT INTO sys_user (username, password, role_id, dept_id, dept_name) VALUES
                                                                           ('admin', 'hqh123', 1, 100, '全院汇总'),
                                                                           ('ken101', 'hqh123', 2, 101, '心内科'),
                                                                           ('staff101', 'hqh123', 3, 101, '心内科');