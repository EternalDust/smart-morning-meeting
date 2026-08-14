# 问题清单

分为「代码审查问题」和「整合问题」两部分，分别记录各子系统代码层面的问题，以及收尾整合阶段跨子系统的问题。

## 代码审查问题

拉取全部五个子系统后通读发现的问题，请各自修复后重新推送。

---

## 巴格达（smart-meeting-supervise）

**跑不起来，先修：**

- 仓库里 `smart-meeting-supervise/backend/` 没有 `pom.xml`，Maven 无法编译。你的 pom.xml 在 `midrepo/` 文件夹里，需要移进去
- `dao/` 和 `mapper/` 下面各有一份 `ProblemMapper`、`AssignMapper`、`ProgressMapper`，同名同包会编译冲突，删掉一组
- `sql/init.sql` 第 11 行有个孤立的 `COLLATE utf8mb4_unicode_ci;`，SQL 语法错误，删掉或注释掉

**应该修：**

- 端口改 8084（README 里分配了 8082/8083/8084，你排第四）
- `AuthController` 里 JWT 还是 TODO，login 之后没有 token 返回，前端路由守卫会一直拦在登录页
- 不要自建 `sm_supervise_user` 用户表，用共享表 `sm_gm_members`
- 表名前缀统一 `sm_`，不要加 `supervise_` 中间层
- `Result` 类方法名改成 `ok()` / `fail()`，跟其他子系统保持一致

---

## 杨子亨（smart-approval-manage）

- 端口改 8082（你排第二）
- 你是唯一用 Spring Data JPA（`Repository`）的，其他四人全是 MyBatis-Plus（`Mapper`）。不改也行，但注意后续跨系统调用没法共用 ORM 工具类
- Java 版本改回 8，pom.xml 里现在是 17
- 所有接口被 `JwtInterceptor` 拦了但没有 `/api/auth/login`，前端拿不到 token，加一个登录端点或者开发阶段先把拦截器放通
- 目录名改成 `smart-meeting-approval`，跟 README 保持一致
- `AgendaService.createAiAgenda` 没用传进来的 `deptName` 和 `meetingType`，只是随机拼了一个固定列表

---

## 曹丁兮（smart-data-collection）

- 端口改 8083（你排第三）
- 只有 `data_source_config` 加了 `data_` 前缀，其他表（`raw_data`、`cleaning_rule`、`clean_data`、`anomaly_record`、`smart_tag`）都没加，统一一下
- `sql/init.sql` 创建了 `smart_tag` 表但 Java 里没有对应的实体类
- JWT 工具类写了但没有配置拦截器，API 实际上是公开的
- `AuthController` 登录随便输什么都通过——哪怕不接数据库，至少只接受 README 里列出的测试用户

---

## 黄祺昊（smart-visual-data）

- 库名 `smm_db` 改成 `smart_meeting`，跟其他人统一
- `sm_gm_members` 表定义和别人不一样（你的是操作日志表，别人的是人员表）。保留你的定义但改名 `sm_operation_log`，人员表用共享的
- 端口 OK（8080）

---

## 汪宇涵（smart-report-interaction）

- 包名 `com.smartmeeting` 改成 `com.huadi.smm`
- 端口 OK（8081）

---

## 所有人

- JWT secret 统一 `smart-morning-meeting-2026`，过期 24 小时。各人改自己的 `JwtUtil`
- `application.yml` 里的数据库密码换掉明文，或者本地覆盖别提交
- 前后端各自看一下 `vite.config.js` 的 proxy 端口是否指向自己的后端端口
- 不要自建用户表，用共享表 `sm_gm_members`

---

## 整合问题（收尾阶段待解决）

以下是从源码和数据库梳理出的、阻碍整合的问题，需对应负责人解决或确认。

### 1. 账号角色体系不一致（三套并存）

共享表 `sm_gm_members.role` 目前只有两个值：`1` = 管理层（工号 2 开头）、`2` = 普通医护（工号 1 开头）。

| 子系统 | 当前做法 | 问题 |
|--------|---------|------|
| 汇报交互、审批 | 工号 `startsWith('2')` 判断管理员 | 用工号前缀，未用 role 字段 |
| 督办 | 自定义 4 角色 enum（督办专员/执行责任人/参会人/管理员） | 与共享表 role（仅 1/2 两值）对不上 |
| 可视化 | 独立 `sys_user` 表（roleId） | 账号体系与共享表分离 |

**解决方向**：账号角色全局统一为「管理员 / 参会人」两种，从 JWT + 共享表读取；「发起人 / 主持人 / 督办专员 / 执行责任人」是业务流程角色，由会议、任务、分派决定，不作为账号属性。

### 2. 交互流程未接通（两段孤岛）

| 链路 | 状态 | 说明 |
|------|------|------|
| 审批 → 汇报交互 | ✅ 已接通 | 共享 `sm_meeting_info` + `sm_meeting_attendee`，审批写、汇报读 |
| 汇报 → 督办 | ❌ 未接通 | 督办未读 `sm_meeting_summary` / `sm_meeting_interaction`，问题抽取未实现 |
| 汇报/采集 → 可视化 | ❌ 未接通 | 可视化用 `bi_stat_*` 统计表，未直接读汇报的 `sm_meeting_*` 和采集的 `data_clean_data` |

### 3. 待各负责人确认的问题（请对应负责人直接填写答案）

**督办（巴格达）**
- [ ] 问题来源读汇报的哪个表/接口（当前未接 `sm_meeting_summary`/`sm_meeting_interaction`）？　答：
- [ ] 4 角色 enum 如何与共享表 role（1/2）对应？　答：

**可视化（黄祺昊）**
- [ ] `sys_user` 与共享 `sm_gm_members` 是否统一？　答：
- [ ] 晨会数据读 `bi_stat_meeting`（统计）还是直接读 `sm_meeting_signin/speech/interaction`？两者关系？　答：
- [ ] 采集的 `data_clean_data` 如何进大屏（当前未读）？　答：

**数据采集（曹丁兮）**
- [ ] `data_clean_data` 的字段维度？　答：
- [ ] 与晨会数据的关联键（会议 ID / 科室）？　答：

**审批（杨子亨）**
- [ ] 审批流程中发起人、审核人是否不同人？　答：
- [ ] 发起人/主持人在表中如何标记？　答：
