package com.huadi.smm.workbench.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huadi.smm.common.entity.Member;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper extends BaseMapper<Member> {
}
