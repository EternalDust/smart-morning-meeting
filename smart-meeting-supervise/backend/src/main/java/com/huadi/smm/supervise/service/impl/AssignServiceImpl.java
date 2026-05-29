package com.huadi.smm.supervise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huadi.smm.supervise.entity.AssignRecord;
import com.huadi.smm.supervise.mapper.AssignMapper;
import com.huadi.smm.supervise.service.AssignService;
import org.springframework.stereotype.Service;

@Service
public class AssignServiceImpl extends ServiceImpl<AssignMapper, AssignRecord>
        implements AssignService {

    @Override
    public AssignRecord autoAssign(Long problemId) {
        AssignRecord record = new AssignRecord();
        record.setProblemId(problemId);
        record.setAssignType(1);
        // 设置默认负责人（根据你的用户表选择一个存在的ID）
        record.setUserId(2L);  // 李医生，role=2（执行责任人）
        this.save(record);
        return record;
    }

    @Override
    public boolean manualAssign(Long problemId, Long userId, Long operatorId, String reason) {
        AssignRecord record = new AssignRecord();
        record.setProblemId(problemId);
        record.setUserId(userId);
        record.setOperatorId(operatorId);
        record.setReason(reason);
        record.setAssignType(2);
        return this.save(record);
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
}