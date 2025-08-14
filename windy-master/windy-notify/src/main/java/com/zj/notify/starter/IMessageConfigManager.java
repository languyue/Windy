package com.zj.notify.starter;


import com.zj.notify.entity.bean.MessagePlatformConfig;
import com.zj.notify.entity.bean.MessageSendConfig;

/**
 * 消息发送配置注册器接口，支持全局和方法级消息配置的注册与获取
 */
public interface IMessageConfigManager {

    /**
     * 获取消息配置
     *
     * @param templateId 模板id
     * @param channelTag 渠道标签
     * @return 消息配置
     */
    MessageSendConfig getMessageSendConfig(String templateId, String channelTag);


    /**
     * 获取默认的消息配置
     *
     * @return 默认的消息配置
     */
    MessagePlatformConfig getDefaultSendConfig();


    /**
     * 注册消息发送配置
     *
     * @param templateId        模板id
     * @param channelTag        模板中渠道的标签
     * @param messageSendConfig token配置
     */
    void registerMessageSendConfig(String templateId, String channelTag, MessageSendConfig messageSendConfig);
}
