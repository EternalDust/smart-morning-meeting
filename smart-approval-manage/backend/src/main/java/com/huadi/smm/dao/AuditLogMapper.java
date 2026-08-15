package com.huadi.smm.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huadi.smm.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}