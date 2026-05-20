package com.huadi.smm.workbench.service;

import java.util.Map;

public interface DataLineageService {

    Map<String, Object> getTableLineage(String tableName);

    Map<String, Object> getRecordLineage(String tableName, Long recordId);

    Map<String, Object> getLineageGraph(String level);
}
