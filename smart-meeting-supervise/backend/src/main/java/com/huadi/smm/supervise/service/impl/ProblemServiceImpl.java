package com.huadi.smm.supervise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huadi.smm.supervise.entity.Problem;
import com.huadi.smm.supervise.mapper.ProblemMapper;
import com.huadi.smm.supervise.service.ProgressService;
import com.huadi.smm.supervise.service.ProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProblemServiceImpl extends ServiceImpl<ProblemMapper, Problem>
        implements ProblemService {

    @Autowired
    private ProgressService progressService;

    @Override
    public Long addProblem(Problem problem) {
        if (!StringUtils.hasText(problem.getTitle())) {
            throw new IllegalArgumentException("标题不能为空");
        }
        if (problem.getTitle().length() > 255) {
            throw new IllegalArgumentException("标题长度不能超过255");
        }
        problem.setStatus(0);
        this.save(problem);
        return problem.getId();
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        Problem problem = new Problem();
        problem.setId(id);
        problem.setStatus(status);
        return this.updateById(problem);
    }

    @Override
    public boolean updateDeadline(Long id, LocalDateTime deadline) {
        Problem problem = new Problem();
        problem.setId(id);
        problem.setDeadline(deadline);
        return this.updateById(problem);
    }

    @Override
    public List<Problem> listMyProblems(Long memberId) {
        return this.list(new LambdaQueryWrapper<Problem>()
                .eq(Problem::getAssigneeId, memberId)
                .in(Problem::getStatus, 1, 2)
                .orderByAsc(Problem::getDeadline)
                .orderByDesc(Problem::getCreateTime));
    }

    @Override
    public void closeProblem(Long id) {
        Problem problem = this.getById(id);
        if (problem == null) {
            throw new IllegalArgumentException("问题不存在");
        }
        if (problem.getStatus() == null || problem.getStatus() != 2) {
            throw new IllegalArgumentException("仅待复查的问题可执行闭环审核");
        }
        Integer current = progressService.getCurrentProgress(id);
        if (current == null || current != 100) {
            throw new IllegalArgumentException("进度未达到100%，不能闭环");
        }
        Problem update = new Problem();
        update.setId(id);
        update.setStatus(3);
        this.updateById(update);
    }

    @Override
    public void deleteProblem(Long id) {
        Problem problem = this.getById(id);
        if (problem == null) {
            throw new IllegalArgumentException("问题不存在");
        }
        this.removeById(id);
    }
}
