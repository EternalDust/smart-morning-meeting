# 晨会智能汇报与实时交互子系统 设计文档

日期: 2026-05-19 | 作者: 汪宇涵 | 版本: Build1.0

## 1. 范围

### 本次构建（前后端技术栈）

- 参会签到：扫码签到、手动签到、签到统计、异常提醒
- 会议汇报：发言内容录入编辑、汇报历史、会议摘要拼装、文档导出
- 实时交互：在线提问、意见反馈、投票表决、WebSocket 实时推送

### 不纳入本次

- 人脸识别签到（AI）、语音实时转写（大模型）、摘要自动生成（大模型）
- Flink/Spark 实时数据处理、鸿蒙移动端适配

## 2. 技术栈

- 后端：Java8 + SpringBoot 2.7 + MyBatis-Plus + SpringSecurity + JWT + WebSocket
- 前端：Vue3 + ElementPlus + Vite + Pinia + Axios
- 数据库：MySQL 8.0
- 接口规范：RESTful JSON，前缀 `/api/meeting/*`，JWT 认证

## 3. 项目结构

```
smart-report-interaction/
├── backend/
│   ├── SmartMeetingApplication.java
│   ├── controller/
│   │   ├── SignController.java       /api/meeting/sign/*
│   │   ├── ReportController.java     汇报 CRUD
│   │   ├── InteractionController.java /api/meeting/interaction/*
│   │   └── RealtimeController.java   WebSocket /api/meeting/realtime/push
│   ├── service/
│   │   ├── SignService.java
│   │   ├── ReportService.java
│   │   ├── InteractionService.java
│   │   └── WebSocketService.java
│   ├── dao/          MyBatis-Plus Mapper
│   ├── entity/
│   ├── config/       WebSocketConfig, SecurityConfig
│   └── common/       Result, PageResult
│
├── frontend/
│   └── src/
│       ├── views/    SignIn.vue, MeetingReport.vue, Interaction.vue
│       ├── components/
│       ├── composables/  useWebSocket
│       ├── router/
│       ├── api/      axios 封装
│       └── stores/   Pinia
│
└── sql/
    └── init.sql
```

## 4. 数据库设计（5 表）

基于数据库设计说明书 `sm_` 前缀表：

| # | 表名 | 字段 |
|---|------|------|
| 1 | sm_meeting_info | id, title, host_id, start_time, end_time, status, create_time |
| 2 | sm_meeting_signin | id, meeting_id FK, user_id, sign_time, sign_type(1扫码2手动), sign_status |
| 3 | sm_meeting_speech | id, meeting_id FK, speaker_id, content longtext, speech_time, key_points |
| 4 | sm_meeting_interaction | id, meeting_id FK, user_id, interact_type(1提问2反馈3投票), content, reply, create_time |
| 5 | sm_meeting_summary | id, meeting_id FK, summary longtext, create_time |

依赖外部表：meeting_info, meeting_agenda, meeting_attendee, user_info（由审批子系统和通用模块提供）

## 5. API 接口

### 签到

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/meeting/sign/in | 参会人员签到 |
| GET | /api/meeting/sign/list/{meetingId} | 查询签到记录 |

### 汇报

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/meeting/speech/save | 保存发言记录 |
| GET | /api/meeting/speech/list/{meetingId} | 获取发言历史 |
| PUT | /api/meeting/speech/{id} | 编辑发言 |
| POST | /api/meeting/summary/save | 保存会议摘要 |
| GET | /api/meeting/summary/{meetingId} | 获取摘要 |
| GET | /api/meeting/summary/export/{meetingId} | 导出摘要文档 |

### 交互

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/meeting/interaction/message | 发送互动消息 |
| GET | /api/meeting/interaction/list/{meetingId} | 获取互动列表 |
| POST | /api/meeting/interaction/vote/create | 发起投票 |
| POST | /api/meeting/interaction/vote/cast | 投票 |
| GET | /api/meeting/interaction/vote/result/{voteId} | 投票结果 |

### 实时推送

| 方法 | 路径 | 说明 |
|------|------|------|
| WS | /api/meeting/realtime/push | WebSocket 实时推送签到/交互更新 |

## 6. 前端页面

### 签到页 (SignIn.vue)

- 左右分栏布局
- 左栏：签到输入区 + 统计卡片（已签到/迟到/缺席/应到）+ 操作按钮
- 右栏：签到记录表（表格内滚动，表头 sticky）

### 汇报页 (MeetingReport.vue)

- 左右分栏布局
- 左栏：议程标签切换 + 当前发言人信息 + 发言录入 + 汇报记录列表（滚动）
- 右栏：会议摘要实时拼装 + 导出按钮

### 互动页 (Interaction.vue)

- 左右分栏布局
- 左栏：消息类型筛选 + 消息流（提问/反馈/投票，滚动）+ 发送区
- 右栏：进行中投票 + 互动统计

## 7. 设计系统

| 维度 | 值 |
|------|-----|
| 风格 | 企业极简 Flat |
| 主色 | #2563EB（蓝） |
| 状态色 | #059669 成功 / #D97706 警告 / #DC2626 异常 |
| 背景 | #F8FAFC / 卡片 #FFFFFF |
| 文字 | #1E293B 主 / #475569 辅 |
| 圆角 | 6px 卡片 / 4px 控件 |
| 阴影 | 0 1px 2px rgba(0,0,0,.06) |
| 字体 | Microsoft YaHei / PingFang SC, 14px, line-height 1.5 |

## 8. 与其他子系统的集成

- 接收：议程数据、参会人员数据（来自审批子系统）
- 提供：签到记录、交互数据（供数据分析和督办子系统查询）
- 接口格式：RESTful JSON，JWT 认证
