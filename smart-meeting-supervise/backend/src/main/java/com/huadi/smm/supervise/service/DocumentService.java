package com.huadi.smm.supervise.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.huadi.smm.supervise.entity.Document;
import java.util.List;

public interface DocumentService extends IService<Document> {

    /**
     * 生成督办文书
     * @param problemId 问题ID
     * @param docType 文书类型：1督办单 2整改通知书 3闭环报告
     * @return 生成的文书内容
     */
    String generateDocument(Long problemId, Integer docType);

    /**
     * 审核文书
     * @param id 文书ID
     * @param status 审核状态：1通过 2驳回
     * @return 是否成功
     */
    boolean auditDocument(Long id, Integer status);

    /**
     * 查询问题的所有文书
     * @param problemId 问题ID
     * @return 文书列表
     */
    List<Document> getDocumentsByProblemId(Long problemId);
}