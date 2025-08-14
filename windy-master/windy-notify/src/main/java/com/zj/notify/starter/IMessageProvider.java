package com.zj.notify.starter;

import com.zj.notify.entity.bean.MessageContentModel;
import com.zj.notify.entity.bean.MessageReceiveConfig;
import com.zj.notify.entity.bean.MessageResp;
import com.zj.notify.entity.bean.MessageSendConfig;

import java.util.function.BiConsumer;

/**
 * @author Violet
 * @describe 消息提供者
 * @since 2025/7/19 23:49
 */
public interface IMessageProvider {

    default String futureName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 发送消息
     *
     * @param messageSendConfig    消息发送配置信息
     * @param messageContent       消息内容
     * @param messageReceiveConfig 消息接收配置
     * @return MessageResp 消息发送结果
     */
    MessageResp sendMessage(MessageSendConfig messageSendConfig, MessageContentModel messageContent,
                            MessageReceiveConfig messageReceiveConfig);


    /**
     * 异步发送消息
     *
     * @param messageSendConfig    消息发送配置信息
     * @param messageContent       消息内容
     * @param messageReceiveConfig 消息接收配置
     * @param callbackAction       消息发送完成的回调动作
     */
    void sendMessageSync(MessageSendConfig messageSendConfig, MessageContentModel messageContent,
                         MessageReceiveConfig messageReceiveConfig, BiConsumer<? super Void, ? super Throwable> callbackAction);
}
