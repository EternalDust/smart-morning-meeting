# 数字医疗智慧晨会协同与决策支撑平台

五个人，五个子系统，一个 MySQL 数据库。

## 项目结构

```
smart-morning-meeting/
├── smart-report-interaction/   # 晨会智能汇报与实时交互（汪宇涵）
├── smart-visual-data/          # 大数据分析与可视化决策（黄祺昊）
├── TBD/                        # 晨会全流程审批与议程管理（李昱希）
├── TBD/                        # 医疗多源数据采集与治理（曹丁兮）
└── TBD/                        # 晨会问题督办与闭环管理（巴格达）
```

## 跑起来

每个子系统自带前后端，互不干扰。

**后端：**

```bash
cd 子系统目录/backend
./mvnw spring-boot:run
```

**前端：**

```bash
cd 子系统目录/frontend
npm install
npm run dev
```

**数据库：** 执行 `子系统目录/sql/init.sql`。

当前端口分配：

| 子系统 | 后端端口 |
|--------|---------|
| smart-report-interaction | 8081 |
| smart-visual-data | 8080 |

## 开发规范

提交代码前先看一眼。

### 包名

统一 `com.huadi.smm`。已经用了别的的找时间改过来。

### API 响应格式

```json
{
  "success": true,
  "code": 200,
  "msg": "success",
  "data": {}
}
```

`code` 跟 HTTP 状态码走。`msg` 失败时写清楚原因。

### 数据库

库名统一 `smart_meeting`，字符集 `utf8mb4`。

表名前缀按子系统分：

| 前缀 | 归属 |
|------|------|
| `sm_` | 晨会业务（审批、签到、汇报、交互、督办） |
| `bi_` | 大数据分析（统计、预警、可视化） |
| `data_` | 数据采集与治理 |

共享表写进 `sql/init.sql`，各自子系统的表自己维护。改共享表之前群里说一声。

### 目录

每个子系统保持一样的结构：

```
子系统名/
├── backend/
│   ├── pom.xml
│   ├── src/main/java/com/huadi/smm/
│   └── src/main/resources/application.yml
├── frontend/
│   └── src/
└── sql/
    └── init.sql
```

### Git

- 根目录 `.gitignore` 已忽略 `node_modules/`、`target/`
- 一个子系统一个文件夹，只加自己的，不要 `git add .`
