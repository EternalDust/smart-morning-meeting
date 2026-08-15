package com.huadi.smm.supervise.ai;

/**
 * AI 能力抽象：配了 API Key 走真实大模型，没配走内置实现。
 */
public interface AiClient {

    /**
     * 生成文本。未配置 API Key 或调用失败时返回 null，由调用方降级处理。
     */
    String chat(String systemPrompt, String userPrompt);
}
