# 问题清单

收尾整合阶段待解决、待各负责人确认的问题。

## 1. 账号角色体系不一致（三套并存）

共享表 `sm_gm_members.role` 目前只有两个值：`1` = 管理层（工号 2 开头）、`2` = 普通医护（工号 1 开头）。

| 子系统 | 当前做法 | 问题 |
|--------|---------|------|
| 汇报交互、审批 | 工号 `startsWith('2')` 判断管理员 | 用工号前缀，未用 role 字段 |
| 督办 | 自定义 4 角色 enum（督办专员/执行责任人/参会人/管理员） | 与共享表 role（仅 1/2 两值）对不上 |
| 可视化 | 已统一到共享 `sm_gm_members`（role=1 全院 / role=2 本科室） | 已随问题①修复，私有 `sys_user` 已删除 |

**解决方向**：账号角色全局统一为「管理员 / 参会人」两种，从 JWT + 共享表读取；「发起人 / 主持人 / 督办专员 / 执行责任人」是业务流程角色，由会议、任务、分派决定，不作为账号属性。

## 2. 交互流程未接通（两段孤岛）

| 链路 | 状态 | 说明 |
|------|------|------|
| 审批 → 汇报交互 | ✅ 已接通 | 共享 `sm_meeting_info` + `sm_meeting_attendee`，审批写、汇报读 |
| 汇报 → 督办 | ✅ 已接通 | 巴格达已实现 `POST /api/supervise/problem/import-meeting`，经汇报读接口把互动提问/反馈导为问题、摘要作跟进事项（契约已核对） |
| 汇报/采集 → 可视化 | ✅ 已接通 | 可视化大屏已直接聚合 `sm_meeting_info/attendee/signin/speech/interaction`、`sm_problem` 与采集的 `data_clean_data`（质量分），并补 `data_clean_data` 演示种子与 Spark 离线聚合 |

## 3. 待各负责人确认的问题（请直接填写答案）

**督办（巴格达）**
- [ ] 问题来源读汇报的哪个表/接口（当前未接 `sm_meeting_summary`/`sm_meeting_interaction`）？　答：
  走汇报交互的读接口，督办后端新增“从会议导入问题”——输入会议 ID，自动把该会议的「提问+反馈」互动消息转成问题（来源标记为自动采集），摘要作为附加问题素材；同时保留手动录入兜底。这样既满足“问题从晨会来，不是手动输入”，演示链路也能完整串起来。
走汇报交互的 RESTful 读接口（经网关 /api/report/** 转发到 8081），不直连库：GET /api/report/meeting/interaction/list/{meetingId}?type=1（提问）、type=2（反馈）、GET /api/report/meeting/summary/export/{meetingId}（AI 会议摘要）。对应共享表 sm_meeting_interaction、sm_meeting_summary。督办侧已实现 POST /api/supervise/problem/import-meeting?meetingId=，把提问/反馈生成问题（source_type=1 自动采集），摘要作为跟进事项；sm_problem 增加 meeting_id 来源列并按「会议+标题」去重，手动录入保留兜底。
- [ ] 4 角色 enum 如何与共享表 role（1/2）对应？　答：
  已废弃 4 角色 enum，账号角色统一为「管理员 / 参会人」：管理员 = 工号 2 开头（对应共享表 role=1 管理层），参会人 = 工号 1 开头（对应 role=2 普通医护），判断从 JWT 的 sub（工号）取，共享表 role 作辅助。“执行责任人 / 督办专员”改为业务流程角色：执行责任人由任务分派产生（从共享表 role=2 科室人员中选，写入 sm_problem.assignee_id），不作为账号属性；进度上报按「当前负责人 + JWT 身份」校验，仅执行责任人可上报，管理员可代录。

**可视化（黄祺昊）**
- [x] `sys_user` 与共享 `sm_gm_members` 是否统一？　答：
- 已统一。可视化已删除私有 `sys_user`，登录/鉴权改走共享表 `sm_gm_members`（JWT sub=工号，密钥/有效期与全平台一致）。数据范围权限从共享 role 映射：`role=1` 管理层 → 全院数据，`role=2` 普通医护 → 仅本科室（`UserContext` 统一解析）；复盘报告等"中层及以上"能力由 role=1 判定。
- [x] 晨会数据读 `bi_stat_meeting`（统计）还是直接读 `sm_meeting_signin/speech/interaction`？两者关系？　答：
- 已改为直接聚合汇报明细表。大屏 `/api/dashboard/trend`（参会率趋势）、`/api/dashboard/meeting-overview`（应到/实到/参会率/发言/互动/质量分）、`/api/dashboard/issues-distribution`（问题部门分布）由 `DashboardAggregateService` 直接按"日期 + 科室"聚合 `sm_meeting_info/attendee/signin/speech/interaction` 与 `sm_problem`，取"有数据的最近 N 天"出图，空数据回退演示值。
  `bi_stat_*` 统计表保留为兼容出口（`/api/dashboard/base-level/data` 仍可读）；明细 → 聚合的上下游关系不变，Spark 离线聚合（`spark_batch_offline.py`）继续承担指标集市写入。
- [x] 采集的 `data_clean_data` 如何进大屏（当前未读）？　答：
- 已接入（离线链路先行）。`/meeting-overview` 的"医疗质量分"已直接聚合 `data_clean_data.quality_score`（按日期平均、科室过滤）；`smart-visual-data/sql/init.sql` 提供演示种子（2026-06 工作日，与 `sm_meeting_*` 日期对齐）；`spark_batch_offline.py` 新增 `data_clean_data → bi_stat_medical` 离线聚合（就诊人次/平均质量分/质量优良率/平均年龄，pymysql 直连、DB 不可用时兜底）。
  关联键按采集（曹丁兮）答复：`data_clean_data.department` → `sm_gm_members.dept`。实时链路（Kafka → Spark Streaming → bi_warn_record）保持原有 mock 链路，后续采集侧接通 Kafka 后可复用。

**数据采集（曹丁兮）**
- [ ] `data_clean_data` 的字段维度？　答：
- 清洗后标准数据表，记录粒度 = 一次就诊/诊疗记录（按 `patient_id + visit_time` 唯一去重）。
  字段：patient_id（患者）、visit_time（就诊时间，标准化为 yyyy-MM-dd HH:mm:ss）、age / gender（人口学，缺失填 -1 / 统一男/女）、
  diagnosis（诊疗，缺失填"未知"）、department（科室）、doctor_id（接诊医生）、quality_score（质量分：完整性40%+一致性30%+有效性30%，≥60 合格）、
  id / create_time（元信息）。
- [ ] 与晨会数据的关联键（会议 ID / 科室）？　答：
- 当前关联键是 `department`（科室）。`data_clean_data.department` 对应 `sm_gm_members.dept`（科室），
  晨会按科室参会/汇报，由此间接关联到会议：`department = sm_gm_members.dept → sm_meeting_attendee（参会人员）→ sm_meeting_info（会议）`。
  科室即大屏按科室展示指标、晨会按科室汇报的聚合维度。表内暂无 meeting_id 字段，如需精确到某次会议可补列（对应 sm_meeting_info.id）。

**审批（杨子亨）**
- [ ] 审批流程中发起人、审核人是否不同人？　答：
- 是不同人。
  发起人 = 科室负责人/主持人（创建会议的人），审核人 = 上级管理层（工号 2 开头）。流程为：发起人创建 → 提交审核 →
  审核人审批通过/驳回 → 发布后进入签到环节。
- [ ] 发起人/主持人在表中如何标记？　答：
- 用 sm_meeting_info 现有字段区分。
  creator_id = 发起人（创建会议的人）；host_id =
  会议主持人（通常与发起人同一人，特殊情况下可不同）。无需新增字段，代码里统一从 JWT 读工号回填到这两个字段。
