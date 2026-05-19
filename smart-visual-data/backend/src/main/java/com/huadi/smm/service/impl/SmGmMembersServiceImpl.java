package com.huadi.smm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huadi.smm.entity.SmGmMembers;
import com.huadi.smm.mapper.SmGmMembersMapper;
import com.huadi.smm.service.SmGmMembersService;
import org.springframework.stereotype.Service;

@Service
public class SmGmMembersServiceImpl extends ServiceImpl<SmGmMembersMapper, SmGmMembers> implements SmGmMembersService {
}
