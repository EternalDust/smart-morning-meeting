package com.huadi.smm.supervise.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.huadi.smm.supervise.entity.Problem;
import java.time.LocalDateTime;

public interface ProblemService extends IService<Problem> {
    Long addProblem(Problem problem);
    boolean updateStatus(Long id, Integer status);

    /**
     * 更新问题截止时间
     */
    boolean updateDeadline(Long id, LocalDateTime deadline);
}
