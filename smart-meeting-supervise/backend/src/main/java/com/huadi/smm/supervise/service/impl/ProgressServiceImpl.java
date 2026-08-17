package com.huadi.smm.supervise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huadi.smm.supervise.entity.Problem;
import com.huadi.smm.supervise.entity.ProgressRecord;
import com.huadi.smm.supervise.entity.User;
import com.huadi.smm.supervise.mapper.ProblemMapper;
import com.huadi.smm.supervise.mapper.ProgressMapper;
import com.huadi.smm.supervise.mapper.UserMapper;
import com.huadi.smm.supervise.service.ProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProgressServiceImpl extends ServiceImpl<ProgressMapper, ProgressRecord>
        implements ProgressService {

    @Autowired
    private ProblemMapper problemMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean submitProgress(ProgressRecord record) {
        // 参数校验
        if (record.getProblemId() == null) {
            throw new IllegalArgumentException("问题ID不能为空");
        }
        if (record.getProgress() == null || record.getProgress() < 0 || record.getProgress() > 100) {
            throw new IllegalArgumentException("进度值必须在0-100之间");
        }
        // 进度只能递增：新上报值不能低于当前进度
        Integer current = getCurrentProgress(record.getProblemId());
        if (current != null && record.getProgress() < current) {
            throw new IllegalArgumentException("进度不能低于当前进度 " + current + "%");
        }
        // 权限校验：上报人必须是当前执行责任人（或管理员），未登录/演示场景（reporterId 为空）放行
        if (record.getReporterId() != null) {
            Problem problem = problemMapper.selectById(record.getProblemId());
            if (problem != null && problem.getStatus() != null && problem.getStatus() == 3) {
                throw new IllegalArgumentException("问题已闭环，不能再上报进度");
            }
            if (problem != null && problem.getAssigneeId() != null
                    && !problem.getAssigneeId().equals(record.getReporterId())) {
                User reporter = userMapper.selectById(record.getReporterId());
                boolean isAdmin = reporter != null && reporter.getUserId() != null
                        && reporter.getUserId().startsWith("2");
                if (!isAdmin) {
                    throw new IllegalArgumentException("仅当前执行责任人可上报进度");
                }
            }
        }
        boolean saved = this.save(record);
        // 状态与最新进度保持一致：
        // 进度达到100% → 待复查；已待复查但最新进度不足100% → 退回处理中
        if (saved && record.getProgress() != null) {
            Problem problem = problemMapper.selectById(record.getProblemId());
            if (problem != null && problem.getStatus() != null) {
                Problem update = new Problem();
                update.setId(problem.getId());
                if (record.getProgress() == 100 && problem.getStatus() == 1) {
                    update.setStatus(2);
                    problemMapper.updateById(update);
                } else if (record.getProgress() < 100 && problem.getStatus() == 2) {
                    update.setStatus(1);
                    problemMapper.updateById(update);
                }
            }
        }
        return saved;
    }

    @Override
    public List<ProgressRecord> getProgressHistory(Long problemId) {
        LambdaQueryWrapper<ProgressRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProgressRecord::getProblemId, problemId)
                .orderByDesc(ProgressRecord::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public Integer getCurrentProgress(Long problemId) {
        LambdaQueryWrapper<ProgressRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProgressRecord::getProblemId, problemId)
                .orderByDesc(ProgressRecord::getCreateTime)
                .orderByDesc(ProgressRecord::getId)
                .last("LIMIT 1");
        ProgressRecord record = this.getOne(wrapper);
        return record != null ? record.getProgress() : 0;
    }
}
