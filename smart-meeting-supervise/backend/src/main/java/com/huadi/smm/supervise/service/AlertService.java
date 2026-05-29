package com.huadi.smm.supervise.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.huadi.smm.supervise.entity.AlertConfig;

public interface AlertService extends IService<AlertConfig> {
    void checkAndSendAlert();
}