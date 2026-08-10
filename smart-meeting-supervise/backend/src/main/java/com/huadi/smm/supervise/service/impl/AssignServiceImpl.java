package com.huadi.smm.supervise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huadi.smm.supervise.entity.AssignRecord;
import com.huadi.smm.supervise.entity.Problem;
import com.huadi.smm.supervise.entity.User;
import com.huadi.smm.supervise.mapper.AssignMapper;
import com.huadi.smm.supervise.mapper.ProblemMapper;
import com.huadi.smm.supervise.mapper.UserMapper;
import com.huadi.smm.supervise.service.AssignService;
import com.huadi.smm.supervise.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssignServiceImpl extends ServiceImpl<AssignMapper, AssignRecord>
        implements AssignService {

    @Autowired
    private ProblemMapper problemMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public AssignRecord autoAssign(Long problemId) {
        Problem problem = problemMapper.selectById(problemId);
        if (problem == null) {
            throw new IllegalArgumentException("问题不存在");
        }
        if (problem.getStatus() != null && problem.getStatus() != 0) {
            throw new IllegalArgumentException("该问题已分派，如需调整请使用人工改派");
        }

        List<User> users = listAssignableUserEntities();
        if (users.isEmpty()) {
            throw new IllegalStateException("暂无可分配的执行责任人");
        }

        User target = pickLeastLoaded(users);
        return doAssign(problem, target, 1, null, "系统自动分派（负载均衡）");
    }

    @Override
    @Transactional
    public boolean manualAssign(Long problemId, Long userId, Long operatorId, String reason) {
        Problem problem = problemMapper.selectById(problemId);
        if (problem == null) {
            throw new IllegalArgumentException("问题不存在");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("负责人不存在");
        }
        if (user.getRole() == null || user.getRole() != 2) {
            throw new IllegalArgumentException("只能分派给执行责任人（科室人员）");
        }
        if (user.getStatus() != null && user.getStatus() != 1) {
            throw new IllegalArgumentException("该负责人已被禁用");
        }

        doAssign(problem, user, 2, operatorId, reason);
        return true;
    }

    @Override
    public Long getCurrentAssignee(Long problemId) {
        LambdaQueryWrapper<AssignRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssignRecord::getProblemId, problemId)
                .orderByDesc(AssignRecord::getCreateTime)
                .last("LIMIT 1");
        AssignRecord record = this.getOne(wrapper);
        return record != null ? record.getUserId() : null;
    }

    @Override
    public List<UserVo> listAssignableUsers() {
        return listAssignableUserEntities().stream()
                .map(this::toVo)
                .collect(Collectors.toList());
    }

    /**
     * 分派并回写问题：写入负责人，状态从“待分派”变为“处理中”
     */
    private AssignRecord doAssign(Problem problem, User target, Integer assignType,
                                  Long operatorId, String reason) {
        AssignRecord record = new AssignRecord();
        record.setProblemId(problem.getId());
        record.setUserId(target.getId());
        record.setAssignType(assignType);
        record.setOperatorId(operatorId);
        record.setReason(reason);
        this.save(record);

        Problem update = new Problem();
        update.setId(problem.getId());
        update.setAssigneeId(target.getId());
        if (problem.getStatus() == null || problem.getStatus() == 0) {
            update.setStatus(1);
        }
        problemMapper.updateById(update);
        return record;
    }

    /**
     * 负载均衡：选择当前“处理中/待复查”任务最少的人
     */
    private User pickLeastLoaded(List<User> users) {
        User best = null;
        long bestLoad = Long.MAX_VALUE;
        for (User user : users) {
            long load = problemMapper.selectCount(new LambdaQueryWrapper<Problem>()
                    .eq(Problem::getAssigneeId, user.getId())
                    .in(Problem::getStatus, 1, 2));
            if (load < bestLoad) {
                bestLoad = load;
                best = user;
            }
        }
        return best;
    }

    private List<User> listAssignableUserEntities() {
        return userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getRole, 2)
                .eq(User::getStatus, 1)
                .orderByAsc(User::getId));
    }

    private UserVo toVo(User user) {
        UserVo vo = new UserVo();
        vo.setId(user.getId());
        vo.setUserId(user.getUserId());
        vo.setName(user.getName());
        vo.setDept(user.getDept());
        vo.setRole(user.getRole());
        return vo;
    }
}
