package com.huadi.smm.supervise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huadi.smm.supervise.entity.Problem;
import org.apache.ibatis.annotations.Mapper;
// import org.apache.ibatis.annotations.Select;  // 删除这行
import java.util.List;
import java.util.Map;

@Mapper
public interface ProblemMapper extends BaseMapper<Problem> {

    List<Map<String, Object>> statisticsByStatus();  // 只保留方法声明

    List<Map<String, Object>> statisticsByCategory();  // 只保留方法声明
}