package com.huadi.smm.supervise.service.impl;  // 改成 smm，不是 sem

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huadi.smm.supervise.entity.AlertConfig;
import com.huadi.smm.supervise.mapper.AlertConfigMapper;
import com.huadi.smm.supervise.service.AlertService;
import org.springframework.stereotype.Service;

@Service
public class AlertServiceImpl extends ServiceImpl<AlertConfigMapper, AlertConfig>
        implements AlertService {

    @Override
    public void checkAndSendAlert() {
        // TODO: 定时任务检查临期/逾期问题并发送预警
    }
}