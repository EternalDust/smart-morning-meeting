# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概况

数字医疗智慧晨会平台，五个子系统（Spring Boot + Vue 3），共享 MySQL `smart_meeting`。每人独立开发一个子系统，通过 RESTful API 互相调用。团队分工、Git 规范详见 README.md。

## 整体架构

```
frontend-shell (:5000 / :8085)  ← 统一登录 + API 网关
smart-report-interaction (:5174 / :8081)   ← 汪宇涵
smart-visual-data (:5173 / :8080)
smart-approval-manage (:5175 / :8082)
smart-data-collection (:5176 / :8083)
smart-meeting-supervise (:5177 / :8084)
```

子系统间数据流：审批 → 汇报交互 → 督办，数据采集 → 可视化。

### API 网关

Shell 后端 `GatewayConfig` 路由映射：

| 路径前缀 | 转发目标 |
|---------|---------|
| `/api/report/**` | `http://127.0.0.1:8081` |
| `/api/approval/**` | `http://127.0.0.1:8082` |
| `/api/supervise/**` | `http://127.0.0.1:8084` |
| `/api/visual/**` | `http://127.0.0.1:8080` |
| `/api/collection/**` | `http://127.0.0.1:8083` |

关键踩坑：路径重写时不能丢失 `/api` 前缀（否则 Security `permitAll` 不放行返回 403）；目标地址用 `127.0.0.1` 不用 `localhost`（IPv6 问题）；RestTemplate 必须设 no-op `ResponseErrorHandler`。

### 认证

`POST /api/auth/login` → JWT（secret = `smart-morning-meeting-2026`，24h 过期）→ 网关透传 → 各子系统自行校验。WebSocket 直连 `ws://host:8081/api/meeting/realtime/push/{meetingId}`，不绕网关。

### 当前状态

鸿蒙 App 已创建，设计文档在 `smart-report-interaction/docs/superpowers/`。后端服务 8080-8085 可正常启动。前端通过 Shell :5000 统一入口，URL 传 token 集成。演示模式 JWT 拦截器已放通。

## 常用命令

```bash
# 后端
cd <子系统>/backend
./mvnw spring-boot:run          # Maven wrapper 自带

# 前端
cd <子系统>/frontend
npm install && npm run dev

# Shell 后端（:8085）
cd frontend-shell/backend
./mvnw compile -q               # 快速编译检查
./mvnw spring-boot:run

# 数据库密码用环境变量，禁止写明文
# Windows: $env:DB_PASSWORD="你的密码"
# application.yml: password: ${DB_PASSWORD}
```

## 目录结构

```
<子系统>/
├── backend/src/main/java/com/huadi/smm/
│   ├── controller/  # REST 接口
│   ├── service/     # 业务逻辑
│   ├── dao/         # MyBatis-Plus Mapper
│   ├── entity/      # 实体类（@TableName 映射表名）
│   ├── config/      # Security、JWT、WebSocket
│   ├── ws/          # WebSocket 端点
│   └── common/      # Result<T> 统一响应
├── frontend/src/
│   ├── views/ api/ router/ stores/ components/
├── sql/init.sql
└── docs/superpowers/{specs,plans}/
```

## API 与数据库

- 统一响应：`{ success, code, msg, data }`，`Result.ok(data)` / `Result.fail(code, msg)`
- 数据库：`smart_meeting`，utf8mb4，表前缀 `sm_`/`bi_`/`data_`
- 共享表（改结构需协调）：`sm_gm_members`、`sm_meeting_info`、`meeting_attendee`、`meeting_agenda`、`sm_org`
- smart-approval-manage 用 Spring Data JPA，其余四个用 MyBatis-Plus 3.5.x

## 开发流程

本项目用 superpowers 流程：`brainstorming` → `writing-plans` → `subagent-driven-development`。调用 skill 直接 `Skill` 工具，不用先 Read。

MCP 工具：`github`（Issue/PR）、`context7`（框架文档）、`sequential-thinking`（设计决策）、`playwright`（浏览器测试 Web 前端，不测原生 App）。

## 注意事项

- Git 只加自己的子系统目录，禁止 `git add .`；禁止提交 `node_modules/`、`target/`、明文数据库密码
- `mvn` 不在 PATH 时用各子系统自带的 `mvnw.cmd`（Windows）或 `mvnw`（Unix）
- data-collection 排除了 Kafka 自动配置和 Flink/Spark 编译，不要擅自加回来
- Shell 后端先 `./mvnw install -DskipTests` 再 `spring-boot:run`（缺少本地仓库依赖会编译失败）
- JAXB 缺失（Java 9+）→ pom.xml 加 `javax.xml.bind:jaxb-api:2.3.1`；Shell 和 data-collection 已补上
- 设计文档统一放 `docs/superpowers/`，不另开目录

## 常用踩坑

- **端口被占**：`netstat -ano | grep :端口 | awk ... | xargs taskkill //F //PID`
- **Git 代理**：仓库代理 `http://127.0.0.1:7897`，代理未启动时 pull/push 失败，绕过：`git -c http.proxy= -c https.proxy= pull`
- **MySQL 中文乱码**：执行 init-all.sql 加 `--default-character-set=utf8mb4`
- **data-collection 启动**：多模块（common/data-collection/data-cleaning/data-labeling/workbench/startup），先 `mvn install -DskipTests`
- **演示账号**：工号 2001/123456 = 杨辉（管理员），工号 1001-1012/123456 = 医护人员

## 子系统集成（答辩演示模式）

Shell `Login.vue` 登录后从 URL `?token=` 传给各子系统前端。各子系统 JWT 拦截器和前端路由守卫已注释/删除（演示放通），JwtUtil 保留可随时恢复。所有子系统 JWT secret 统一为 `smart-morning-meeting-2026`。

## 报告撰写规范

撰写实习报告等正式文档时遵循以下规则，避免 AI 生成痕迹：

- **最小化冒号和分号**。用句号或逗号代替，或调整句式避免列表式罗列。冒号尤其容易出现在"主要职责：""包括：""如下："等位置，应改写为完整句子
- **不用英文冒号 `:` 替代中文冒号 `：`**
- **一段到底**。摘要、Abstract、各小节正文尽量保持连续段落，不频繁换行
- **不讲技术细节**。面向用户描述系统功能和价值，不堆砌技术名词（Spring Boot、Vue、MyBatis-Plus 等框架名不出现在摘要中）
- **以"我"为主语**。主动句式描述自己做了什么，不使用"本文""本报告""该系统"等第三人称
- **关键词3-5个**，用中文逗号分隔，不用分号或顿号
- **参考学长报告风格**。荆勃豪、乔怡斐、曾祥科的最终报告已提取在 `finalrepo/__full_*.txt`，写作前参考其句式、篇幅和措辞

## 参考文件索引

- 鸿蒙开发详情：`docs/superpowers/harmonyos-dev.md`
- 大数据集群环境：`docs/superpowers/bigdata-cluster.md`
- 设计文档：`smart-report-interaction/docs/superpowers/{specs,plans}/`
- 大数据端开发引导：`大数据端开发引导.md`
- 统一数据库脚本：`sql/init-all.sql`
- 代码审查问题清单：`issues.md`
- 中期报告：`midrepo/企业实习中期报告-2023090915013-汪宇涵.docx`
