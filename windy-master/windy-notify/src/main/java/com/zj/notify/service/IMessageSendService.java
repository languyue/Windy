package com.zj.notify.service;

public interface IMessageSendService {

    /**
     * 通过模板发送消息
     *
     * @param templateId         模板ID
     * @param methodInvokeResult 方法执行结果
     * @param contextParams      上下文参数
     * @return 发送结果
     */
    boolean sendMessageFromTemplate(String templateId, boolean methodInvokeResult, String contextParams);

    /**
     * 发送消息
     *
     * @param content 消息内容
     * @return 发送结果
     */
    boolean sendMessage(String content);
}
