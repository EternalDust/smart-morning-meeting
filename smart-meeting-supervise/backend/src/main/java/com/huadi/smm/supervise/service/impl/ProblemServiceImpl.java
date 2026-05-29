package com.huadi.smm.supervise.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huadi.smm.supervise.entity.Problem;
import com.huadi.smm.supervise.mapper.ProblemMapper;
import com.huadi.smm.supervise.service.ProblemService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ProblemServiceImpl extends ServiceImpl<ProblemMapper, Problem>
        implements ProblemService {

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
}