package com.huadi.smm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 大模型推理服务配置（Qwen / DeepSeek 等 OpenAI 兼容接口）
 * 对应 application.yml 中 ai.llm.*
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai.llm")
public class AiLlmProperties {

    /** 大模型 HTTP 服务地址，留空则使用模板模拟生成，保证演示可用 */
    private String baseUrl = "";

    /** 模型名称 */
    private String model = "qwen-plus";

    /** API Key（通过环境变量 AI_LLM_API_KEY 注入，不写明文） */
    private String apiKey = "";

    /** 连接超时（秒） */
    private int connectTimeoutSeconds = 10;

    /** 读取超时（秒） */
    private int readTimeoutSeconds = 60;

    /** 生成温度 */
    private double temperature = 0.7;

    /** 最大生成 token 数 */
    private int maxTokens = 1500;

    /** 请求失败 / 校验不通过时的最大重试次数 */
    private int maxRetry = 3;
}
