package com.huadi.smm.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    /**
     * 基层操作人员接口：如各科室晨会填报、自己的数据趋势
     */
    @GetMapping("/base-level/data")
    @PreAuthorize("hasAnyRole('BASE_LEVEL', 'MID_LEVEL', 'HIGH_LEVEL')")
    public String getBaseLevelData() {
        return "返回基层操作人员所需的页面数据（聚焦本部门明细）";
    }

    /**
     * 中层管理人员接口：如查看部门汇总，一般和中等风险预警
     */
    @GetMapping("/mid-level/summary")
    @PreAuthorize("hasAnyRole('MID_LEVEL', 'HIGH_LEVEL')")
    public String getMidLevelData() {
        return "返回中层管理人员汇总数据与风险预警";
    }

    /**
     * 高层决策人员接口：全平台宏观数据、大模型给出的AI建议
     */
    @GetMapping("/high-level/decision-support")
    @PreAuthorize("hasRole('HIGH_LEVEL')")
    public String getHighLevelData() {
        return "返回高层全平台宏观数据与大模型智能辅助决策建议";
    }
}
