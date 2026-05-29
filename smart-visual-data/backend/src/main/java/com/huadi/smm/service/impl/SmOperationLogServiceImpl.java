package com.huadi.smm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huadi.smm.entity.SmOperationLog;
import com.huadi.smm.mapper.SmOperationLogMapper;
import com.huadi.smm.service.SmOperationLogService;
import org.springframework.stereotype.Service;

@Service
public class SmOperationLogServiceImpl extends ServiceImpl<SmOperationLogMapper, SmOperationLog> implements SmOperationLogService {
}
