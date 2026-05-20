package com.huadi.smm.collection.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huadi.smm.common.entity.DataSourceConfig;

public interface DataSourceService {

    Long addDataSource(DataSourceConfig config);

    Page<DataSourceConfig> pageList(int page, int size);

    DataSourceConfig getById(Long id);

    void updateDataSource(DataSourceConfig config);

    void deleteById(Long id);

    boolean testConnection(Long sourceId);
}
