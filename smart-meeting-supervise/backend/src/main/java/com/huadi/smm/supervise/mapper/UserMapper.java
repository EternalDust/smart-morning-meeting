package com.huadi.smm.supervise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huadi.smm.supervise.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM sm_gm_members WHERE account = #{account}")
    User findByAccount(String account);
}