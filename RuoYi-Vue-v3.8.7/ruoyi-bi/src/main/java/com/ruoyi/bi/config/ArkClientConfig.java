package com.ruoyi.bi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 火山方舟Ark API配置
 *
 * 直接通过HTTP REST调用Ark API（Chat Completions格式）
 * 不依赖OpenAI Java SDK，避免版本兼容问题
 *
 * @author ruoyi-bi
 */
@Configuration
public class ArkClientConfig {

    private static final Logger log = LoggerFactory.getLogger(ArkClientConfig.class);

    @Value("${ai.ark.base-url:https://ark.cn-beijing.volces.com/api/v3}")
    private String baseUrl;

    @Value("${ai.ark.api-key:${ARK_API_KEY:}}")
    private String apiKey;

    @Value("${ai.ark.model:deepseek-v4-flash-260425}")
    private String model;

    @Value("${ai.ark.embedding-model:bge-m3}")
    private String embeddingModel;

    @Value("${ai.ark.timeout-ms:30000}")
    private int timeoutMs;

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getModel() {
        return model;
    }

    public String getEmbeddingModel() {
        return embeddingModel;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    /**
     * 检查API Key是否已配置
     */
    public boolean isEnabled() {
        return apiKey != null && !apiKey.trim().isEmpty() && !"${ARK_API_KEY:}".equals(apiKey);
    }
}
