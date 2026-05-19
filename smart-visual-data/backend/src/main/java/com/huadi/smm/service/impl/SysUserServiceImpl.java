package com.huadi.smm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huadi.smm.entity.SysUser;
import com.huadi.smm.mapper.SysUserMapper;
import com.huadi.smm.service.SysUserService;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
}