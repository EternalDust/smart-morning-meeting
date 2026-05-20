package com.smartmeeting.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartmeeting.entity.Attendee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AttendeeMapper extends BaseMapper<Attendee> {
}
