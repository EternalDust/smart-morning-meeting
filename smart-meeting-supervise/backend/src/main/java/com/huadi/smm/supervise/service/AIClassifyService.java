package com.huadi.smm.supervise.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class AIClassifyService {

    public Map<String, Integer> classify(String problemText) {
        Map<String, Integer> result = new HashMap<>();

        if (problemText.contains("系统") || problemText.contains("服务器") || problemText.contains("网络")) {
            result.put("category", 2);  // 运维类
            result.put("riskLevel", 2); // 重要
        } else if (problemText.contains("药品") || problemText.contains("病历") || problemText.contains("设备")) {
            result.put("category", 1);  // 医疗类
            result.put("riskLevel", 3); // 紧急
        } else {
            result.put("category", 3);  // 管理类
            result.put("riskLevel", 1); // 一般
        }
        return result;
    }
}