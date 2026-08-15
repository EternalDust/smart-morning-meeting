package com.huadi.smm.supervise.ai.impl;

import com.huadi.smm.supervise.ai.AiClient;

/**
 * 未配置 API Key 时的内置实现：返回 null，文书生成降级为内置模板。
 */
public class MockAiClient implements AiClient {
    @Override
    public String chat(String systemPrompt, String userPrompt) {
        return null;
    }
}
