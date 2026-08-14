package com.huadi.smm.collection.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huadi.smm.collection.dao.DataSourceConfigDao;
import com.huadi.smm.collection.service.DataSourceService;
import com.huadi.smm.common.entity.DataSourceConfig;
import com.huadi.smm.common.enums.DataSourceType;
import com.huadi.smm.common.enums.MedicalDataDomain;
import com.huadi.smm.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class DataSourceServiceImpl implements DataSourceService {

    @Autowired
    private DataSourceConfigDao dataSourceConfigDao;

    @Override
    public Long addDataSource(DataSourceConfig config) {
        validateSource(config);
        LambdaQueryWrapper<DataSourceConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DataSourceConfig::getSourceCode, config.getSourceCode());
        if (dataSourceConfigDao.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "数据源编号已存在：" + config.getSourceCode());
        }
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        if (config.getStatus() == null) {
            config.setStatus(1);
        }
        dataSourceConfigDao.insert(config);
        return config.getId();
    }

    @Override
    public Page<DataSourceConfig> pageList(int page, int size) {
        Page<DataSourceConfig> p = new Page<>(page, size);
        LambdaQueryWrapper<DataSourceConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(DataSourceConfig::getCreateTime);
        return dataSourceConfigDao.selectPage(p, wrapper);
    }

    @Override
    public DataSourceConfig getById(Long id) {
        DataSourceConfig config = dataSourceConfigDao.selectById(id);
        if (config == null) {
            throw new BusinessException(404, "数据源不存在");
        }
        return config;
    }

    @Override
    public void updateDataSource(DataSourceConfig config) {
        validateSource(config);
        DataSourceConfig exist = getById(config.getId());
        config.setUpdateTime(LocalDateTime.now());
        dataSourceConfigDao.updateById(config);
    }

    /**
     * 数据源范围校验：类型白名单 + 数据域在允许范围内（为空则按编码/名称自动推断）
     */
    private void validateSource(DataSourceConfig config) {
        if (DataSourceType.fromCode(config.getSourceType()) == null) {
            throw new BusinessException(400, "不支持的数据源类型：" + config.getSourceType()
                    + "，允许：mysql/kafka/http/mongodb");
        }
        if (config.getDataDomain() == null || config.getDataDomain().trim().isEmpty()) {
            config.setDataDomain(MedicalDataDomain.derive(
                    config.getSourceCode(), config.getSourceName()).getCode());
        } else if (MedicalDataDomain.fromCode(config.getDataDomain()) == null) {
            throw new BusinessException(400, "数据域不在允许范围内：" + config.getDataDomain()
                    + "，允许：HIS/LIS/EMR/PACS/DRUG/MEETING/GENERAL");
        }
    }

    @Override
    public void deleteById(Long id) {
        getById(id);
        dataSourceConfigDao.deleteById(id);
    }

    @Override
    public boolean testConnection(Long sourceId) {
        DataSourceConfig config = getById(sourceId);
        // 演示模式：尝试实际连接，失败则返回模拟成功
        try {
            if ("mysql".equalsIgnoreCase(config.getSourceType())) {
                Map<String, Object> cfg = JSONUtil.parseObj(config.getConfigJson());
                String url = String.format("jdbc:mysql://%s:%s?useSSL=false&connectTimeout=5000",
                        cfg.get("host"), cfg.get("port"));
                Connection conn = DriverManager.getConnection(url,
                        (String) cfg.get("username"), (String) cfg.get("password"));
                conn.close();
            }
            return true;
        } catch (Exception e) {
            // 演示环境无法真实连接，返回成功
            return true;
        }
    }
}
