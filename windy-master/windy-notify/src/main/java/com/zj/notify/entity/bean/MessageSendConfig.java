package com.zj.notify.entity.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Violet
 * @describe 消息配置实体类
 * @since 2025/7/20 01:06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSendConfig {
    
    /**
     * 配置键
     */
    private String configKey;
    
    /**
     * Webhook URL
     */
    private String webhook;
    
    /**
     * 访问令牌
     */
    private String accessToken;
    
    /**
     * 密钥
     */
    private String secret;
    
    /**
     * 其他配置参数
     */
    @Builder.Default
    private Map<String, String> extraParams = new HashMap<>();
    
    /**
     * 是否启用
     */
    @Builder.Default
    private boolean enabled = true;
    
    /**
     * 超时时间（毫秒）
     */
    private Integer timeout;
    
    /**
     * 重试次数
     */
    private Integer retryCount;
    
    /**
     * 添加额外参数
     */
    public void addExtraParam(String key, String value) {
        if (extraParams == null) {
            extraParams = new HashMap<>();
        }
        extraParams.put(key, value);
    }
} 