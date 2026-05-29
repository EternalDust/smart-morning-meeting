package com.huadi.smm.supervise.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huadi.smm.supervise.entity.Problem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProblemMapper extends BaseMapper<Problem> {

    @Select("SELECT status, COUNT(*) as count FROM sm_supervise_problem GROUP BY status")
    List<Map<String, Object>> statisticsByStatus();
}
