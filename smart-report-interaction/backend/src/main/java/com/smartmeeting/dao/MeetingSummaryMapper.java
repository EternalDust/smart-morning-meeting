package com.smartmeeting.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartmeeting.entity.MeetingSummary;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MeetingSummaryMapper extends BaseMapper<MeetingSummary> {
}
