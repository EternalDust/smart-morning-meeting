package com.huadi.smm.supervise.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.huadi.smm.supervise.entity.AssignRecord;
import com.huadi.smm.supervise.entity.Problem;
import com.huadi.smm.supervise.vo.UserVo;

import java.util.List;

public interface AssignService extends IService<AssignRecord> {

    /**
     * AI自动分派
     * @param problemId 问题ID
     * @return 分派记录
     */
    AssignRecord autoAssign(Long problemId);

    /**
     * 人工改派
     * @param problemId 问题ID
     * @param userId 新负责人ID
     * @param operatorId 操作人ID
     * @param reason 改派原因
     * @return 是否成功
     */
    boolean manualAssign(Long problemId, Long userId, Long operatorId, String reason);

    /**
     * 获取问题的当前负责人
     * @param problemId 问题ID
     * @return 负责人ID
     */
    Long getCurrentAssignee(Long problemId);

    /**
     * 查询可分配的执行责任人列表
     * @return 启用状态的科室人员（role=2）
     */
    List<UserVo> listAssignableUsers();
}
