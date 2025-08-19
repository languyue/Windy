package com.zj.notify.starter;


/**
 * 消息发送配置注册器接口，支持全局和方法级消息配置的注册与获取
 */
public interface IMessageConfigRegister {

    /**
     * 执行渠道配置注册
     */
    void messageChannelRegister();
}
