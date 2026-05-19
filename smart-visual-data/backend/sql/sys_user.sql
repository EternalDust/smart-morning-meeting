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
-- 密码明文暂定为 hqh123，为了快速演示不加密
INSERT INTO sys_user (username, password, role_id, dept_id, dept_name) VALUES 
('admin', 'hqh123', 1, 100, '全院汇总'),
('ken101', 'hqh123', 2, 101, '心内科'),
('staff101', 'hqh123', 3, 101, '心内科');
