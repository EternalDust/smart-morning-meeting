# 问题清单

收尾整合阶段待解决、待各负责人确认的问题。

## 1. 账号角色体系不一致（三套并存）

共享表 `sm_gm_members.role` 目前只有两个值：`1` = 管理层（工号 2 开头）、`2` = 普通医护（工号 1 开头）。

| 子系统 | 当前做法 | 问题 |
|--------|---------|------|
| 汇报交互、审批 | 工号 `startsWith('2')` 判断管理员 | 用工号前缀，未用 role 字段 |
| 督办 | 自定义 4 角色 enum（督办专员/执行责任人/参会人/管理员） | 与共享表 role（仅 1/2 两值）对不上 |
| 可视化 | 独立 `sys_user` 表（roleId） | 账号体系与共享表分离 |

**解决方向**：账号角色全局统一为「管理员 / 参会人」两种，从 JWT + 共享表读取；「发起人 / 主持人 / 督办专员 / 执行责任人」是业务流程角色，由会议、任务、分派决定，不作为账号属性。

## 2. 交互流程未接通（两段孤岛）

| 链路 | 状态 | 说明 |
|------|------|------|
| 审批 → 汇报交互 | ✅ 已接通 | 共享 `sm_meeting_info` + `sm_meeting_attendee`，审批写、汇报读 |
| 汇报 → 督办 | ✅ 已接通 | 巴格达已实现 `POST /api/supervise/problem/import-meeting`，经汇报读接口把互动提问/反馈导为问题、摘要作跟进事项（契约已核对） |
| 汇报/采集 → 可视化 | ❌ 未接通 | 可视化仍消费 `bi_stat_*` mock 统计表，未直接读汇报的 `sm_meeting_*` 和采集的 `data_clean_data`；黄祺昊只给了接入规划，代码未落地 |

## 3. 待各负责人确认的问题（请直接填写答案）

**督办（巴格达）**
- [ ] 问题来源读汇报的哪个表/接口（当前未接 `sm_meeting_summary`/`sm_meeting_interaction`）？　答：
  走汇报交互的读接口，督办后端新增“从会议导入问题”——输入会议 ID，自动把该会议的「提问+反馈」互动消息转成问题（来源标记为自动采集），摘要作为附加问题素材；同时保留手动录入兜底。这样既满足“问题从晨会来，不是手动输入”，演示链路也能完整串起来。
走汇报交互的 RESTful 读接口（经网关 /api/report/** 转发到 8081），不直连库：GET /api/report/meeting/interaction/list/{meetingId}?type=1（提问）、type=2（反馈）、GET /api/report/meeting/summary/export/{meetingId}（AI 会议摘要）。对应共享表 sm_meeting_interaction、sm_meeting_summary。督办侧已实现 POST /api/supervise/problem/import-meeting?meetingId=，把提问/反馈生成问题（source_type=1 自动采集），摘要作为跟进事项；sm_problem 增加 meeting_id 来源列并按「会议+标题」去重，手动录入保留兜底。
- [ ] 4 角色 enum 如何与共享表 role（1/2）对应？　答：
  已废弃 4 角色 enum，账号角色统一为「管理员 / 参会人」：管理员 = 工号 2 开头（对应共享表 role=1 管理层），参会人 = 工号 1 开头（对应 role=2 普通医护），判断从 JWT 的 sub（工号）取，共享表 role 作辅助。“执行责任人 / 督办专员”改为业务流程角色：执行责任人由任务分派产生（从共享表 role=2 科室人员中选，写入 sm_problem.assignee_id），不作为账号属性；进度上报按「当前负责人 + JWT 身份」校验，仅执行责任人可上报，管理员可代录。

**可视化（黄祺昊）**
- [ ] `sys_user` 与共享 `sm_gm_members` 是否统一？　答：
- 当前未统一。可视化独立维护 `sys_user` 表（role_id 1/2/3 = 高层/中层/基层），登录鉴权走本子系统自己的 JWT（secret `smart-morning-meeting-2026`，24h 过期），与共享表 `sm_gm_members.role`（1 管理层 / 2 普通医护）两套并存。
  建议整合：账号与鉴权统一到共享表 + JWT，可视化侧不再单独建账号表；"高层/中层/基层"作为展示/数据范围权限，从共享 role 映射得到：`role=1`（管理层）→ 中层及以上（可看复盘报告 / 全院数据），`role=2`（普通医护）→ 基层（只看本科室）。既满足"权限分级"要求，又与共享账号体系对齐。
- [ ] 晨会数据读 `bi_stat_meeting`（统计）还是直接读 `sm_meeting_signin/speech/interaction`？两者关系？　答：
- 当前读统计结果 `bi_stat_meeting`，不直接读汇报交互明细表。大屏参会率趋势查 `bi_stat_meeting`、部门分布查 `bi_stat_supervise`、实时预警查 `bi_warn_record`（由实时异常检测模块写入）。
  两者是"明细 → 聚合"的上下游关系：`sm_meeting_signin/speech/interaction` 明细由汇报交互子系统产生，大数据分析子系统用 Spark 实时/离线任务按"日期 + 科室"聚合成 `bi_stat_*` 统计事实表，大屏只消费聚合结果，避免直接读明细带来的高耦合与查询压力。目前 `bi_stat_meeting` 由 mock 脚本填充（`mock_data_7days.sql` / `/api/dashboard/test-insert`），接真实数据时由 Spark 批处理从明细按日按科室写入即可。
- [ ] 采集的 `data_clean_data` 如何进大屏（当前未读）？　答：
- 当前未接入。现有链路为自造 mock 数据进 Kafka → Spark Streaming → 实时异常检测 → 写 `bi_warn_record`，未消费采集的 `data_clean_data`。
  规划接入路径（可并行）：
  1. 实时链路：采集侧将 `data_clean_data` 发到 Kafka（复用现有 topic 或新开），Spark Streaming 消费后做指标聚合与异常检测，结果写 `bi_stat_*` / `bi_warn_record`，大屏 WebSocket 实时刷新；
  2. 离线链路：Spark 离线批处理按 T+1 从 `data_clean_data` 聚合写入 `bi_stat_meeting` / `bi_stat_medical`，供趋势 / 分布图表查询。
  关联键按采集（曹丁兮）答复：`data_clean_data.department` → `sm_gm_members.dept` → `meeting_attendee` → `sm_meeting_info`；如需精确到某次会议，需在 `data_clean_data` 补 `meeting_id` 列。确认字段与关联键后，我在大数据侧补对应的 Kafka 消费者 / 批处理读取模块即可打通。

**数据采集（曹丁兮）**
- [ ] `data_clean_data` 的字段维度？　答：
- 清洗后标准数据表，记录粒度 = 一次就诊/诊疗记录（按 `patient_id + visit_time` 唯一去重）。
  字段：patient_id（患者）、visit_time（就诊时间，标准化为 yyyy-MM-dd HH:mm:ss）、age / gender（人口学，缺失填 -1 / 统一男/女）、
  diagnosis（诊疗，缺失填"未知"）、department（科室）、doctor_id（接诊医生）、quality_score（质量分：完整性40%+一致性30%+有效性30%，≥60 合格）、
  id / create_time（元信息）。
- [ ] 与晨会数据的关联键（会议 ID / 科室）？　答：
- 当前关联键是 `department`（科室）。`data_clean_data.department` 对应 `sm_gm_members.dept`（科室），
  晨会按科室参会/汇报，由此间接关联到会议：`department = sm_gm_members.dept → meeting_attendee（参会人员）→ sm_meeting_info（会议）`。
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
