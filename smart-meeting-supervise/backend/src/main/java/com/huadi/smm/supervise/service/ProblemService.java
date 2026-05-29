package com.huadi.smm.supervise.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.huadi.smm.supervise.entity.Problem;

public interface ProblemService extends IService<Problem> {
    Long addProblem(Problem problem);
    boolean updateStatus(Long id, Integer status);
}