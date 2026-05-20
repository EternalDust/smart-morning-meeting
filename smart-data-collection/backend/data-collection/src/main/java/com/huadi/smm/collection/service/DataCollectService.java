package com.huadi.smm.collection.service;

import java.util.Map;

public interface DataCollectService {

    void sendToKafka(String source, Map<String, Object> data);

    Map<String, Object> getCollectStats();
}
