# 数字医疗智慧晨会协同与决策支撑平台

五个子系统组一个晨会管理平台。从发起晨会、签到、实时汇报互动，到问题督办和大屏数据展示，覆盖晨会整个生命周期。

## 子系统

每个子系统是一个人独立开发的前后端子项目，通过 RESTful API 互相调用。

| 子系统 | 英文目录 | 负责人 | 进度 |
|--------|---------|--------|------|
| 晨会全流程审批与议程管理 | `smart-meeting-approval` | 杨子亨 | 待提交 |
| 医疗多源数据采集与治理 | `smart-data-collection` | 曹丁兮 | 待提交 |
| 晨会智能汇报与实时交互 | `smart-report-interaction` | 汪宇涵 | 已提交 |
| 晨会问题督办与闭环管理 | `smart-meeting-supervise` | 巴格达 | 待提交 |
| 大数据分析与可视化决策 | `smart-visual-data` | 黄祺昊 | 已提交 |

子系统之间的数据流：

```
审批子系统 ──→ 议程、参会人 ──→ 汇报交互子系统
                                    │
                              签到、交互数据
                                    │
                                    ├──→ 督办子系统
                                    │
数据采集子系统 ──→ 标准数据 ──→ 大数据大屏子系统
```

## 环境

| 工具 | 版本 | 干嘛用 |
|------|------|--------|
| JDK | 8+ | 后端编译运行 |
| Node.js | 16+ | 前端开发 |
| MySQL | 8.0 | 数据存储 |
| Maven | 3.9（或直接用项目自带的 mvnw） | 后端构建 |

## 从零跑起来

先拉代码，再初始化数据库，然后启动前后端。顺序无所谓，端口不冲突就行。

```bash
git clone https://github.com/EternalDust/smart-morning-meeting.git
cd smart-morning-meeting
```

### 1. 数据库

打开 MySQL，执行你负责的子系统的 `sql/init.sql`。不同子系统可能共用同一个库（`smart_meeting`），所以如果你不是第一个建库的人，跳过 `CREATE DATABASE` 那一行就行。

### 2. 后端

```bash
cd 你的子系统/backend

# 第一次用 Maven wrapper 会自动下载 Maven
./mvnw spring-boot:run
```

Maven wrapper 第一次运行会下载依赖，等几分钟。以后就快了。

### 3. 前端

```bash
cd 你的子系统/frontend
npm install    # 第一次才需要
npm run dev
```

浏览器打开终端输出的地址，通常是 `http://localhost:5173`。前端请求自动代理到本子系统的后端端口，不用手动配。

### 4. 当前端口

| 子系统 | 后端 | 前端 dev |
|--------|------|---------|
| smart-report-interaction | 8081 | 5173 |
| smart-visual-data | 8080 | 5173（如果同时跑，Vite 会自动切到 5174） |

后续三个子系统建议从 8082 开始依次往下排。`application.yml` 里改 `server.port` 就行。

## 开发规范

### 包名

统一 `com.huadi.smm`。已经用了别的的自己改。

### API 响应

不管后端怎么内部处理，返给前端的 JSON 长这样：

```json
{
  "success": true,
  "code": 200,
  "msg": "success",
  "data": {}
}
```

`success: false` 的时候 `code` 写错误码，`msg` 写错误原因。不要只返回字符串，也别每个接口格式不一样。

认证用 JWT，请求头带 `Authorization: Bearer <token>`。开发阶段可以先放通，别删掉 JWT 占位就行。

### 数据库

**库名：** `smart_meeting`，字符集 `utf8mb4`。

**表名前缀：**

| 前缀 | 意思 | 例子 |
|------|------|------|
| `sm_` | 晨会业务 | `sm_meeting_signin` |
| `bi_` | 大数据分析 | `bi_stat_meeting` |
| `data_` | 数据采集治理 | `data_source_config` |

**共享表：** 以下表属于多个子系统共用的，改结构之前群里说。

| 表 | 用途 | 当前维护者 |
|---|---|---|
| `sm_gm_members` | 人员信息（工号、姓名、部门） | 汪宇涵 |
| `sm_meeting_info` | 晨会主表 | 杨子亨（待建） |
| `meeting_attendee` | 参会人员名单 | 汪宇涵 |
| `meeting_agenda` | 议程表 | 杨子亨（待建） |
| `sm_org` | 组织/科室结构 | 待定 |

自己子系统的业务表各人自己建、自己维护，放进各自的 `sql/init.sql`。

### 目录结构

每个子系统的骨架：

```
子系统名/
├── backend/
│   ├── pom.xml
│   ├── src/main/java/com/huadi/smm/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── dao/
│   │   ├── entity/
│   │   └── config/
│   └── src/main/resources/
│       └── application.yml
├── frontend/
│   └── src/
│       ├── views/
│       ├── api/
│       ├── router/
│       └── stores/
└── sql/
    └── init.sql
```

### Git

项目里已经配好了 `.gitignore`（`node_modules`、`target`、IDE 文件都不会进仓库）。

日常操作：

```bash
# 每次开始干活前
git pull --rebase origin master

# 只加自己的目录
git add smart-xxx/

# 提交
git commit -m "feat: 干了什么"

# push 之前再拉一次，防止别人先推了
git pull --rebase origin master
git push origin master
```

**遇到 push 被拒？** 说明远端有人先推了。这种情况：

```bash
git pull --rebase origin master
# 有冲突就解冲突
# git add 冲突文件
# git rebase --continue
git push origin master
```

只要每人只改自己的子系统目录，很少会遇到冲突。万一有，坐下来一起对着文件解。

**不要：**
- 不要 `git add .` 然后 commit。会把别人的目录和临时文件也卷进去
- 不要 `git push --force`。除非你真的知道你在干什么
- 不要把自己子系统的 `node_modules/` 或 `target/` 加进仓库

`application.yml` 里的数据库密码不要写真实密码推上去，用个占位符或者本地覆盖。
