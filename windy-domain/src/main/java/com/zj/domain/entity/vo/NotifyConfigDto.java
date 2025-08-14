package com.zj.domain.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Data
public class NotifyConfigDto {

    /**
     * 平台
     */
    private String platform;


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
}
