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

    /**
     * 复查通过，闭环问题（仅待复查且进度100%的问题可闭环）
     */
    void closeProblem(Long id);

    /**
     * 删除问题（级联删除分派/进度/文书记录）
     */
    void deleteProblem(Long id);
}
