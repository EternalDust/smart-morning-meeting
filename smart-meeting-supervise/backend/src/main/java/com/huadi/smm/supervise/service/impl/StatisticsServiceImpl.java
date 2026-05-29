package com.huadi.smm.supervise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huadi.smm.supervise.entity.Problem;
import com.huadi.smm.supervise.mapper.ProblemMapper;
import com.huadi.smm.supervise.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private ProblemMapper problemMapper;

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> result = new HashMap<>();

        // 总问题数
        long total = problemMapper.selectCount(null);
        result.put("total", total);

        // 按状态统计
        List<Map<String, Object>> byStatus = problemMapper.statisticsByStatus();
        result.put("byStatus", byStatus);

        // 按分类统计
        List<Map<String, Object>> byCategory = problemMapper.statisticsByCategory();
        result.put("byCategory", byCategory);

        // 各状态数量
        long waitAssign = problemMapper.selectCount(new LambdaQueryWrapper<Problem>().eq(Problem::getStatus, 0));
        long processing = problemMapper.selectCount(new LambdaQueryWrapper<Problem>().eq(Problem::getStatus, 1));
        long waitCheck = problemMapper.selectCount(new LambdaQueryWrapper<Problem>().eq(Problem::getStatus, 2));
        long closed = problemMapper.selectCount(new LambdaQueryWrapper<Problem>().eq(Problem::getStatus, 3));

        Map<String, Long> statusDetail = new HashMap<>();
        statusDetail.put("waitAssign", waitAssign);
        statusDetail.put("processing", processing);
        statusDetail.put("waitCheck", waitCheck);
        statusDetail.put("closed", closed);
        result.put("statusDetail", statusDetail);

        return result;
    }
}
