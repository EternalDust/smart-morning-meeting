-- 1. 数据字典与指标维度表
CREATE TABLE `bi_dim_dict` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典id',
  `dict_code` varchar(64) NOT NULL COMMENT '字典编码',
  `dict_name` varchar(255) NOT NULL COMMENT '字典名称',
  `dict_type` varchar(32) DEFAULT NULL COMMENT '字典类型',
  `sort` int DEFAULT NULL COMMENT '排序号',
  `status` int DEFAULT NULL COMMENT '状态',
  `create_time` varchar(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` varchar(20) DEFAULT NULL COMMENT '更新时间',
  `profile` varchar(1000) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典与指标维度表';

-- 2. 晨会统计指标事实表
CREATE TABLE `bi_stat_meeting` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '统计id',
  `stat_date` varchar(255) NOT NULL COMMENT '统计日期',
  `dept_id` bigint DEFAULT NULL COMMENT '科室id',
  `dept_name` varchar(255) DEFAULT NULL COMMENT '科室名称',
  `meeting_count` int DEFAULT NULL COMMENT '晨会场次',
  `should_num` int DEFAULT NULL COMMENT '应到人数',
  `real_num` int DEFAULT NULL COMMENT '实到人数',
  `attend_rate` decimal(5,2) DEFAULT NULL COMMENT '参会率',
  `late_num` int DEFAULT NULL COMMENT '迟到人数',
  `absent_num` int DEFAULT NULL COMMENT '缺席人数',
  `avg_duration` int DEFAULT NULL COMMENT '平均时长',
  `create_time` varchar(20) DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='晨会统计指标事实表';

-- 3. 问题督办统计事实表
CREATE TABLE `bi_stat_supervise` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '统计id',
  `stat_date` varchar(255) NOT NULL COMMENT '统计日期',
  `dept_id` bigint DEFAULT NULL COMMENT '科室id',
  `total_count` int DEFAULT NULL COMMENT '问题总数',
  `solved_count` int DEFAULT NULL COMMENT '已解决',
  `solve_rate` decimal(5,2) DEFAULT NULL COMMENT '解决率',
  `overdue_count` int DEFAULT NULL COMMENT '逾期数',
  `urgent_count` int DEFAULT NULL COMMENT '紧急问题数',
  `avg_handle_hour` int DEFAULT NULL COMMENT '平均处理时长',
  `create_time` varchar(20) DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问题督办统计事实表';

-- 4. 医疗业务指标事实表
CREATE TABLE `bi_stat_medical` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '统计 ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `dept_id` bigint DEFAULT NULL COMMENT '科室 ID',
  `index_code` varchar(64) DEFAULT NULL COMMENT '指标编码',
  `index_name` varchar(255) DEFAULT NULL COMMENT '指标名称',
  `index_value` decimal(18,2) DEFAULT NULL COMMENT '指标值',
  `target_value` decimal(18,2) DEFAULT NULL COMMENT '目标值',
  `complete_rate` decimal(5,2) DEFAULT NULL COMMENT '完成率',
  `create_time` varchar(20) DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医疗业务指标事实表';

-- 5. 异常检测与预警表
CREATE TABLE `bi_warn_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '预警 ID',
  `warn_type` varchar(32) DEFAULT NULL COMMENT '预警类型',
  `warn_level` int DEFAULT NULL COMMENT '预警等级',
  `dept_id` bigint DEFAULT NULL COMMENT '科室 ID',
  `index_code` varchar(64) DEFAULT NULL COMMENT '异常指标',
  `abnormal_value` varchar(64) DEFAULT NULL COMMENT '异常值',
  `threshold_value` varchar(64) DEFAULT NULL COMMENT '阈值',
  `status` int DEFAULT NULL COMMENT '状态',
  `handler_id` bigint DEFAULT NULL COMMENT '处理人',
  `handle_time` varchar(20) DEFAULT NULL COMMENT '处理时间',
  `create_time` varchar(20) DEFAULT NULL COMMENT '生成时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异常检测与预警表';

-- 6. 可视化大屏配置表
CREATE TABLE `bi_chart_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置 ID',
  `chart_code` varchar(64) NOT NULL COMMENT '图表编码',
  `chart_name` varchar(255) DEFAULT NULL COMMENT '图表名称',
  `chart_type` varchar(32) DEFAULT NULL COMMENT '图表类型',
  `source_table` varchar(64) DEFAULT NULL COMMENT '数据源表',
  `role_ids` varchar(512) DEFAULT NULL COMMENT '权限角色',
  `enabled` int DEFAULT NULL COMMENT '是否启用',
  `sort` int DEFAULT NULL COMMENT '排序号',
  `update_time` varchar(20) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='可视化大屏配置表';

-- 7. 平台人员操作日志表 (文档中字段属性属于操作日志)
CREATE TABLE `sm_gm_members` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志 ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户 ID',
  `user_name` varchar(64) DEFAULT NULL COMMENT '用户名',
  `module` varchar(64) DEFAULT NULL COMMENT '操作模块',
  `content` varchar(512) DEFAULT NULL COMMENT '操作内容',
  `ip` varchar(64) DEFAULT NULL COMMENT '操作 IP',
  `create_time` varchar(20) DEFAULT NULL COMMENT '操作时间',
  `result` int DEFAULT NULL COMMENT '执行结果',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台人员(日志)表';
