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
| 汇报 → 督办 | ❌ 未接通 | 督办未读 `sm_meeting_summary` / `sm_meeting_interaction`，问题抽取未实现 |
| 汇报/采集 → 可视化 | ❌ 未接通 | 可视化用 `bi_stat_*` 统计表，未直接读汇报的 `sm_meeting_*` 和采集的 `data_clean_data` |

## 3. 待各负责人确认的问题（请直接填写答案）

**督办（巴格达）**
- [ ] 问题来源读汇报的哪个表/接口（当前未接 `sm_meeting_summary`/`sm_meeting_interaction`）？　答：
- [ ] 4 角色 enum 如何与共享表 role（1/2）对应？　答：

**可视化（黄祺昊）**
- [ ] `sys_user` 与共享 `sm_gm_members` 是否统一？　答：
- [ ] 晨会数据读 `bi_stat_meeting`（统计）还是直接读 `sm_meeting_signin/speech/interaction`？两者关系？　答：
- [ ] 采集的 `data_clean_data` 如何进大屏（当前未读）？　答：

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
