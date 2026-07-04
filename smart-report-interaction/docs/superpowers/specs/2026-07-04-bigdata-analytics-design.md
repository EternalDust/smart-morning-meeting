# 大数据分析模块设计

## 概述

在 smart-report-interaction 子系统内新增大数据分析层。利用 Hadoop 3.2.2 + Spark 2.4.7 集群对晨会历史数据做离线批处理，产出会议趋势、科室对比、个人画像三个维度的分析结果，通过 REST API 和 Vue 前端展示。

## 数据链路

```
MySQL (现有业务表) ──Spark JDBC读取──→ Spark DataFrame 聚合 ──写回──→ MySQL (新分析表)
                                                                       │
                                                                       ▼
                                                               Spring Boot REST API
                                                                       │
                                                                       ▼
                                                               Vue 前端分析页
```

Spark 只读现有业务表，不写。计算结果写入新分析表，后端 API 只做查询。

## 数据库

全部数据来自 smart_meeting 库现有表：sm_meeting_info、sm_meeting_signin、sm_meeting_speech、sm_meeting_interaction、meeting_attendee、sm_gm_members。

### sm_meeting_analytics — 会议维度分析

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO_INCREMENT | |
| meeting_id | BIGINT NOT NULL | 关联 sm_meeting_info |
| meeting_title | VARCHAR(200) | 会议标题 |
| meeting_date | VARCHAR(20) | 会议日期 |
| should_attend | INT | 应到人数（meeting_attendee 计数） |
| actual_attend | INT | 实到人数（signin 去重计数） |
| attend_rate | DECIMAL(5,2) | 出勤率(%) |
| normal_count | INT | 准时签到数(sign_status=0) |
| late_count | INT | 迟到数(sign_status=1) |
| speech_count | INT | 发言数 |
| interaction_count | INT | 互动数 |
| quality_score | DECIMAL(5,2) | 质量评分(0-100) |
| created_at | VARCHAR(20) | |
| updated_at | VARCHAR(20) | |

质量评分公式：出勤率×0.4 + (发言数/应到人数)×100×0.3 + (互动数/应到人数)×100×0.3

### sm_department_analytics — 科室维度分析

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO_INCREMENT | |
| department | VARCHAR(100) | 科室名称（来自 sm_gm_members） |
| meeting_count | INT | 参与会议次数 |
| avg_attend_rate | DECIMAL(5,2) | 平均出勤率(%) |
| total_speech_count | INT | 累计发言数 |
| total_interaction_count | INT | 累计互动数 |
| period_start | VARCHAR(20) | 统计周期起始 |
| period_end | VARCHAR(20) | 统计周期结束 |
| updated_at | VARCHAR(20) | |

### sm_member_analytics — 个人维度分析

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO_INCREMENT | |
| user_id | VARCHAR(20) NOT NULL | 工号 |
| user_name | VARCHAR(50) | 姓名（来自 sm_gm_members） |
| department | VARCHAR(100) | 科室 |
| total_meetings | INT | 应参会次数 |
| attended_count | INT | 实到次数 |
| attend_rate | DECIMAL(5,2) | 出勤率(%) |
| normal_count | INT | 准时次数 |
| late_count | INT | 迟到次数 |
| speech_count | INT | 发言次数 |
| interaction_count | INT | 互动次数 |
| last_updated | VARCHAR(20) | |

## REST API

所有接口沿用项目统一格式 `Result<T>`：`{ success, code, msg, data }`。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/analytics/meeting/{meetingId}` | 单场会议分析 |
| GET | `/api/analytics/meetings/trend` | 会议趋势（多场对比） |
| GET | `/api/analytics/departments` | 科室对比排名 |
| GET | `/api/analytics/member/{userId}` | 个人参会画像 |

不引入新依赖，用项目已有的 Spring Boot 2.7 + MyBatis-Plus 3.5.x。

## Spark 作业

### 技术选型

- Spark 2.4.7（集群已有），Scala 2.11
- 通过 JDBC 读取 MySQL、写回 MySQL
- 独立 Maven 项目，打包 JAR 提交到 Spark 集群

### 位置

```
smart-report-interaction/spark/
├── pom.xml
├── submit.sh
└── src/main/scala/com/huadi/smm/spark/
    └── MeetingAnalyticsJob.scala
```

### 计算逻辑

1. 从 MySQL 读取 6 张业务表到 DataFrame
2. 会议维度：按 meeting_id 分组聚合，计算签到统计、发言数、互动数、质量评分
3. 科室维度：按 department 分组聚合
4. 个人维度：按 user_id 分组聚合
5. 结果通过 JDBC 写入三张分析表

### 提交方式

```bash
spark-submit --master spark://centos6:7077 \
  --class com.huadi.smm.spark.MeetingAnalyticsJob \
  --jars mysql-connector-java-8.0.33.jar \
  target/spark-analytics-1.0.jar
```

按需手动执行，不设定时调度。

## 前端

Vue 页面 `/analytics`，使用 ECharts（项目已有依赖）展示：

- 会议趋势折线图（签到率、发言数、互动数随时间变化）
- 科室对比柱状图（出勤率排名）
- 个人画像卡片

## 目录结构

```
smart-report-interaction/
├── backend/src/main/java/com/huadi/smm/
│   ├── entity/
│   │   ├── MeetingAnalytics.java
│   │   ├── DepartmentAnalytics.java
│   │   └── MemberAnalytics.java
│   ├── dao/
│   │   ├── MeetingAnalyticsMapper.java
│   │   ├── DepartmentAnalyticsMapper.java
│   │   └── MemberAnalyticsMapper.java
│   ├── service/
│   │   └── AnalyticsService.java
│   └── controller/
│       └── AnalyticsController.java
├── spark/                              # 独立 Maven 项目
│   ├── pom.xml
│   ├── submit.sh
│   └── src/main/scala/com/huadi/smm/spark/
│       └── MeetingAnalyticsJob.scala
├── frontend/src/
│   ├── views/Analytics.vue
│   ├── api/analytics.js
│   └── router/index.js                 # 新增 /analytics 路由
└── sql/
    └── analytics_init.sql              # 三张分析表 DDL
```
