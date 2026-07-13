package com.ruoyi.bi.service.llm;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.bi.config.ArkClientConfig;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 大模型调用服务（兼容 OpenAI 协议的统一 AI 网关）
 *
 * 直接通过 HTTP REST 调用网关的 Chat Completions / Embeddings 端点
 * 不依赖 OpenAI Java SDK，避免版本兼容问题
 * 支持 deepseek-v3、deepseek-r1、qwen 系列及 BAAI/bge-m3 等模型
 *
 * @author ruoyi-bi
 */
@Service
public class LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmService.class);

    /** 缓存Key前缀 */
    private static final String CACHE_PREFIX = "ai:chat:";
    /** 缓存过期时间（小时） */
    private static final int CACHE_EXPIRE_HOURS = 1;

    @Autowired
    private ArkClientConfig arkConfig;

    @Autowired
    private RedisCache redisCache;

    /**
     * 调用大模型（同步，NL2SQL场景）
     *
     * @param prompt 用户输入的提示词（作为user message）
     * @return 大模型返回的文本
     */
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public String chat(String prompt) {
        return chat(prompt, 0.1);
    }

    /**
     * 调用大模型（同步）
     *
     * @param prompt       用户输入的提示词
     * @param temperature  温度参数（NL2SQL用0.1，对话用0.3）
     * @return 大模型返回的文本
     */
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public String chat(String prompt, double temperature) {
        checkEnabled();

        // 1. 生成缓存Key（模型 + prompt的MD5）
        String cacheKey = buildCacheKey(prompt, temperature);

        // 2. 先查缓存
        try {
            String cached = redisCache.getCacheObject(cacheKey);
            if (cached != null) {
                log.info("LLM缓存命中，prompt长度：{}", prompt.length());
                return cached;
            }
        } catch (Exception e) {
            log.warn("Redis缓存读取失败，继续调用API：{}", e.getMessage());
        }

        // 3. 缓存未命中，调用API
        try {
            log.debug("调用大模型，prompt长度：{}", prompt.length());

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", arkConfig.getModel());
            requestBody.put("temperature", temperature);
            requestBody.put("max_tokens", 2000);

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            messages.add(userMsg);
            requestBody.put("messages", messages);

            // 发送HTTP请求
            String response = postForJson(arkConfig.getBaseUrl() + "/chat/completions", requestBody);
            String result = parseResponse(response);

            // 4. 存入缓存
            try {
                redisCache.setCacheObject(cacheKey, result, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            } catch (Exception e) {
                log.warn("Redis缓存写入失败，不影响主流程：{}", e.getMessage());
            }

            return result;

        } catch (ResourceAccessException e) {
            log.error("大模型调用超时", e);
            throw new ServiceException("系统响应超时，请稍后再试");
        } catch (HttpClientErrorException e) {
            log.error("大模型调用HTTP错误，状态码：{}", e.getStatusCode(), e);
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED
                    || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new ServiceException("API Key无效或已过期，请联系管理员");
            } else if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new ServiceException("请求过于频繁，请稍后再试");
            } else {
                throw new ServiceException("API调用失败（HTTP " + e.getStatusCode() + "）：" + e.getMessage());
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("大模型调用失败", e);
            throw new ServiceException("大模型调用失败：" + e.getMessage());
        }
    }

    /**
     * 调用大模型（带上下文，对话场景）
     *
     * @param messages 对话历史，每个元素为 Map 包含 role 和 content
     * @return 大模型返回的文本
     */
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public String chatWithHistory(List<Map<String, String>> messages) {
        return chatWithHistory(messages, 0.3);
    }

    /**
     * 调用大模型（带上下文，对话场景）
     */
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public String chatWithHistory(List<Map<String, String>> messages, double temperature) {
        checkEnabled();

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", arkConfig.getModel());
            requestBody.put("temperature", temperature);
            requestBody.put("max_tokens", 2000);
            requestBody.put("messages", messages);

            String response = postForJson(arkConfig.getBaseUrl() + "/chat/completions", requestBody);
            return parseResponse(response);

        } catch (Exception e) {
            log.error("大模型调用失败", e);
            throw new ServiceException("大模型调用失败：" + e.getMessage());
        }
    }

    /**
     * 调用向量化模型（用于RAG）
     *
     * @param texts 待向量化的文本列表
     * @return 向量列表（每个文本对应一个float[]）
     */
    public List<float[]> embed(List<String> texts) {
        checkEnabled();

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", arkConfig.getEmbeddingModel());
            requestBody.put("input", texts);

            String response = postForJson(arkConfig.getBaseUrl() + "/embeddings", requestBody);
            return parseEmbedResponse(response);

        } catch (Exception e) {
            log.error("向量化调用失败", e);
            throw new ServiceException("向量化失败：" + e.getMessage());
        }
    }

    /**
     * 发送POST请求，返回响应JSON字符串
     */
    private String postForJson(String url, Map<String, Object> body) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(arkConfig.getApiKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new ServiceException("AI网关返回错误码：" + response.getStatusCode());
            }
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // 透传HTTP异常，由上层统一处理（区分401/429等）
            throw e;
        } catch (ResourceAccessException e) {
            throw new ServiceException("网络连接超时，请检查网络后重试");
        } catch (RestClientException e) {
            throw new ServiceException("AI网关调用失败：" + e.getMessage());
        }
    }

    /**
     * 构建缓存Key（模型名 + temperature + prompt的MD5）
     */
    private String buildCacheKey(String prompt, double temperature) {
        String raw = arkConfig.getModel() + ":" + temperature + ":" + prompt;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(raw.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder(CACHE_PREFIX);
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            // MD5失败时降级为简单hashCode
            return CACHE_PREFIX + Math.abs(raw.hashCode());
        }
    }

    /**
     * 解析Chat Completions响应，提取content文本
     * 响应格式：{"choices":[{"message":{"content":"..."}}]}
     */
    private String parseResponse(String responseJson) {
        try {
            JSONObject root = JSON.parseObject(responseJson);
            JSONArray choices = root.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                throw new ServiceException("大模型返回为空（无choices）");
            }
            JSONObject message = choices.getJSONObject(0).getJSONObject("message");
            String content = message.getString("content");
            if (content == null || content.trim().isEmpty()) {
                throw new ServiceException("大模型返回内容为空");
            }
            return content.trim();
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("解析大模型返回失败，response={}", responseJson, e);
            throw new ServiceException("解析大模型返回失败：" + e.getMessage());
        }
    }

    /**
     * 解析Embeddings响应，提取向量
     * 响应格式：{"data":[{"embedding":[0.1,0.2,...]}]}
     */
    private List<float[]> parseEmbedResponse(String responseJson) {
        List<float[]> result = new ArrayList<>();
        try {
            JSONObject root = JSON.parseObject(responseJson);
            JSONArray data = root.getJSONArray("data");
            for (int i = 0; i < data.size(); i++) {
                JSONArray embedding = data.getJSONObject(i).getJSONArray("embedding");
                float[] vec = new float[embedding.size()];
                for (int j = 0; j < embedding.size(); j++) {
                    vec[j] = embedding.getFloatValue(j);
                }
                result.add(vec);
            }
            return result;
        } catch (Exception e) {
            log.error("解析向量化返回失败", e);
            throw new ServiceException("解析向量化返回失败：" + e.getMessage());
        }
    }

    /**
     * 测试连接
     */
    public boolean testConnection() {
        try {
            checkEnabled();
            String result = chat("你好");
            return result != null && !result.isEmpty();
        } catch (Exception e) {
            log.error("测试连接失败", e);
            return false;
        }
    }

    private void checkEnabled() {
        if (!arkConfig.isEnabled()) {
            throw new ServiceException("AI大模型未启用：api-key 未配置，请在 application.yml 中设置 ai.ark.api-key");
        }
    }
}
