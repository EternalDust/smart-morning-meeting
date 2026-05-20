package com.huadi.smm.collection.controller;

import com.huadi.smm.common.entity.DataSourceConfig;
import com.huadi.smm.common.entity.R;
import com.huadi.smm.collection.service.DataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/datasource")
public class DataSourceController {

    @Autowired
    private DataSourceService dataSourceService;

    @PostMapping("/add")
    public R addDataSource(@RequestBody DataSourceConfig config) {
        if (config.getSourceCode() == null || config.getSourceCode().isEmpty()) {
            return R.error(400, "数据源编号不能为空");
        }
        if (config.getSourceType() == null || config.getSourceType().isEmpty()) {
            return R.error(400, "数据源类型不能为空");
        }
        return R.ok(dataSourceService.addDataSource(config)).message("数据源添加成功");
    }

    @GetMapping("/list")
    public R listDataSources(@RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "10") int size) {
        return R.ok(dataSourceService.pageList(page, size));
    }

    @GetMapping("/{sourceId}")
    public R getDataSource(@PathVariable Long sourceId) {
        return R.ok(dataSourceService.getById(sourceId));
    }

    @PutMapping("/{sourceId}")
    public R updateDataSource(@PathVariable Long sourceId, @RequestBody DataSourceConfig config) {
        config.setId(sourceId);
        dataSourceService.updateDataSource(config);
        return R.ok().message("数据源更新成功");
    }

    @DeleteMapping("/{sourceId}")
    public R deleteDataSource(@PathVariable Long sourceId) {
        dataSourceService.deleteById(sourceId);
        return R.ok().message("数据源删除成功");
    }

    @PostMapping("/test/{sourceId}")
    public R testConnection(@PathVariable Long sourceId) {
        boolean reachable = dataSourceService.testConnection(sourceId);
        return reachable
                ? R.ok().message("连接测试成功")
                : R.error(400, "连接测试失败，请检查配置");
    }
}
