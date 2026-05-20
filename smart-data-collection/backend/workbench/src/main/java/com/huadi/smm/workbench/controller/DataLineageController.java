package com.huadi.smm.workbench.controller;

import com.huadi.smm.common.entity.R;
import com.huadi.smm.workbench.service.DataLineageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lineage")
public class DataLineageController {

    @Autowired
    private DataLineageService dataLineageService;

    @GetMapping("/table/{tableName}")
    public R getTableLineage(@PathVariable String tableName) {
        return R.ok(dataLineageService.getTableLineage(tableName));
    }

    @GetMapping("/record/{tableName}/{recordId}")
    public R getRecordLineage(@PathVariable String tableName, @PathVariable Long recordId) {
        return R.ok(dataLineageService.getRecordLineage(tableName, recordId));
    }

    @GetMapping("/graph")
    public R getLineageGraph(@RequestParam(defaultValue = "table") String level) {
        return R.ok(dataLineageService.getLineageGraph(level));
    }
}
