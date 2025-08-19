package com.zj.notify.service.support;

import com.zj.notify.entity.bean.MessagePlatformConfig;
import com.zj.notify.entity.bean.MessageSendConfig;
import com.zj.notify.starter.IMessageConfigManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class DefaultMessageConfigManager implements IMessageConfigManager {


    private final Map<String, MessageSendConfig> globalNotifyConfig = new HashMap<>();

    private final Map<String, MessageSendConfig> methodNotifyConfig = new HashMap<>();


    @Override
    public MessageSendConfig getMessageSendConfig(String templateId, String channelTag) {
        String configKey = templateId + "@" + channelTag;
        MessageSendConfig messageSendConfig = methodNotifyConfig.get(configKey);
        if (messageSendConfig != null) {
            return messageSendConfig;
        }

        return globalNotifyConfig.get(channelTag);
    }

    @Override
    public MessagePlatformConfig getDefaultSendConfig() {
        Optional<Map.Entry<String, MessageSendConfig>> optional = globalNotifyConfig.entrySet().stream().findFirst();
        if (!optional.isPresent()) {
            return null;
        }

        Map.Entry<String, MessageSendConfig> entry = optional.get();
        return new MessagePlatformConfig(entry.getKey(), entry.getValue());
    }

    @Override
    public void registerMessageSendConfig(String templateId, String channelTag, MessageSendConfig messageSendConfig) {
        String configKey = templateId + "@" + channelTag;
        methodNotifyConfig.put(configKey, messageSendConfig);
        globalNotifyConfig.putIfAbsent(channelTag, messageSendConfig);
    }
}
