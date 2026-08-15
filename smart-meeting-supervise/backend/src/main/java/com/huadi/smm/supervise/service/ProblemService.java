package com.huadi.smm.supervise.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.huadi.smm.supervise.entity.Problem;
import java.time.LocalDateTime;
import java.util.List;

public interface ProblemService extends IService<Problem> {
    Long addProblem(Problem problem);
    boolean updateStatus(Long id, Integer status);

    /**
     * 更新问题截止时间
     */
    boolean updateDeadline(Long id, LocalDateTime deadline);

    /**
     * 查询某个成员当前负责的处理中/待复查问题
     */
    List<Problem> listMyProblems(Long memberId);
}
