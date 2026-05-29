package com.huadi.smm.supervise.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.huadi.smm.supervise.entity.ProgressRecord;
import java.util.List;

public interface ProgressService extends IService<ProgressRecord> {

    /**
     * 上报进度
     * @param record 进度记录
     * @return 是否成功
     */
    boolean submitProgress(ProgressRecord record);

    /**
     * 查询问题的进度历史
     * @param problemId 问题ID
     * @return 进度记录列表
     */
    List<ProgressRecord> getProgressHistory(Long problemId);

    /**
     * 获取问题当前进度
     * @param problemId 问题ID
     * @return 当前进度百分比
     */
    Integer getCurrentProgress(Long problemId);
}