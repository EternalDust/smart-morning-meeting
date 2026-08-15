package com.huadi.smm.supervise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huadi.smm.supervise.entity.Problem;
import com.huadi.smm.supervise.entity.ProgressRecord;
import com.huadi.smm.supervise.mapper.ProblemMapper;
import com.huadi.smm.supervise.mapper.ProgressMapper;
import com.huadi.smm.supervise.service.ProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProgressServiceImpl extends ServiceImpl<ProgressMapper, ProgressRecord>
        implements ProgressService {

    @Autowired
    private ProblemMapper problemMapper;

    @Override
    public boolean submitProgress(ProgressRecord record) {
        // 参数校验
        if (record.getProblemId() == null) {
            throw new IllegalArgumentException("问题ID不能为空");
        }
        if (record.getProgress() == null || record.getProgress() < 0 || record.getProgress() > 100) {
            throw new IllegalArgumentException("进度值必须在0-100之间");
        }
        boolean saved = this.save(record);
        // 闭环：进度达到100%时，处理中的问题自动进入“待复查”
        if (saved && record.getProgress() != null && record.getProgress() == 100) {
            Problem problem = problemMapper.selectById(record.getProblemId());
            if (problem != null && problem.getStatus() != null && problem.getStatus() == 1) {
                Problem update = new Problem();
                update.setId(problem.getId());
                update.setStatus(2);
                problemMapper.updateById(update);
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
                .last("LIMIT 1");
        ProgressRecord record = this.getOne(wrapper);
        return record != null ? record.getProgress() : 0;
    }
}
