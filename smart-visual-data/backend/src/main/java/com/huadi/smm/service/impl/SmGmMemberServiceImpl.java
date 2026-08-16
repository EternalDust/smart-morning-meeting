package com.huadi.smm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huadi.smm.entity.SmGmMember;
import com.huadi.smm.mapper.SmGmMemberMapper;
import com.huadi.smm.service.SmGmMemberService;
import org.springframework.stereotype.Service;

@Service
public class SmGmMemberServiceImpl extends ServiceImpl<SmGmMemberMapper, SmGmMember> implements SmGmMemberService {
}
