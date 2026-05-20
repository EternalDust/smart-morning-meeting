package com.huadi.smm.labeling.service;

import java.util.List;
import java.util.Map;

public interface SmartLabelService {

    void generateLabels(String entityType);

    List<Map<String, Object>> listLabelsByType(String entityType);

    Map<String, Object> parseUnstructuredText(String text, String recordType);

    List<Map<String, Object>> batchParse(List<String> texts, String recordType);
}
