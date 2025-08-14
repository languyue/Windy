package com.zj.notify.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.zj.notify.entity.bean.*;
import com.zj.notify.entity.enums.ContentNodeEnum;
import com.zj.notify.service.IMessageSendService;
import com.zj.notify.service.strategy.factory.MessageProviderFactory;
import com.zj.notify.service.support.DefaultMessageConfigManager;
import com.zj.notify.service.support.MessageTemplateManager;
import com.zj.notify.service.support.XmlTemplateTemplateParse;
import com.zj.notify.starter.IMessageConfigManager;
import com.zj.notify.starter.IMessageProvider;
import com.zj.notify.starter.IMessageTemplateParse;
import com.zj.notify.starter.IMessageTemplateManager;
import com.zj.notify.utils.SpringSpiFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@DependsOn(value = {"springSpiFactory"})
public class MessageSendService implements IMessageSendService {

    /**
     * 默认解析器,优先从SPI中获取
     */
    private final IMessageTemplateParse messageTemplateParse;

    private final IMessageTemplateManager messageTemplateManager;

    private final IMessageConfigManager messageConfigManager;

    public MessageSendService() {
        messageTemplateParse = SpringSpiFactory.load(IMessageTemplateParse.class, new XmlTemplateTemplateParse());
        messageTemplateManager = SpringSpiFactory.load(IMessageTemplateManager.class, new MessageTemplateManager());
        messageConfigManager = SpringSpiFactory.load(IMessageConfigManager.class, new DefaultMessageConfigManager());
    }

    @Override
    public boolean sendMessageFromTemplate(String templateId, boolean methodInvokeResult, String contextParams) {
        if (StringUtils.isAnyBlank(contextParams, templateId)) {
            return false;
        }

        // get source template content by templateId,  template => com.zj.notify.entity.enums.MessageModuleType.DEPLOY
        ContentNodeEnum contentNodeName = methodInvokeResult ? ContentNodeEnum.SUCCESS : ContentNodeEnum.ERROR;
        MessageTemplatePayload templateContentPayload = messageTemplateManager.getTemplateContent(templateId, contentNodeName.getNodeName());
        if (templateContentPayload == null || StringUtils.isBlank(templateContentPayload.getTemplateContent())) {
            log.warn("MessageSendService sendMessageFromTemplate templateContentPayload is null......");
            return false;
        }

        // get default message platform config,the default config must exist
        MessagePlatformConfig defaultSendConfig = messageConfigManager.getDefaultSendConfig();
        if (defaultSendConfig == null) {
            log.warn("MessageSendService sendMessageFromTemplate defaultSendConfig is null......");
            return false;
        }

        // choose message provider and get default message platform
        String defaultPlatform = defaultSendConfig.getPlatform();
        Optional<IMessageProvider> messageFuture = Optional.ofNullable(MessageProviderFactory.getMessageFuture(defaultPlatform));
        if (!messageFuture.isPresent()) {
            log.warn("MessageSendService sendMessageFromTemplate there is no corresponding message provider......");
            return false;
        }

        MessageSendConfig messageSendConfig = messageConfigManager.getMessageSendConfig(templateId, defaultPlatform);
        if (messageSendConfig == null) {
            log.warn("MessageSendService sendMessageFromTemplate messageSendConfig is null......");
            return false;
        }

        // parse source template content
        Map<String, Object> templateParams = JSON.parseObject(contextParams, new TypeReference<Map<String, Object>>() {});
        String renderContent = messageTemplateParse.parse(templateContentPayload.getTemplateContent(), templateParams);
        if (StringUtils.isBlank(renderContent)) {
            log.warn("MessageSendService sendMessageFromTemplate renderContent is empty, params:{}", JSON.toJSONString(templateContentPayload));
            return false;
        }

        MessageContentModel contentModel = new MessageContentModel(renderContent, templateContentPayload.getTemplateType(), null);
        MessageResp messageResp = messageFuture.get().sendMessage(messageSendConfig, contentModel, null);
        log.info("MessageSendService sendMessageFromTemplate result:{}", JSON.toJSONString(messageResp));
        return true;
    }

    @Override
    public boolean sendMessage(String content) {
        MessagePlatformConfig messagePlatformSendConfig = messageConfigManager.getDefaultSendConfig();
        if (messagePlatformSendConfig == null) {
            log.warn("MessageSendService sendMessage messagePlatformSendConfig is null......");
            return false;
        }

        IMessageProvider messageFuture = MessageProviderFactory.getMessageFuture(messagePlatformSendConfig.getPlatform());
        if (messageFuture == null) {
            log.warn("MessageSendService sendMessage there is no corresponding message provider......");
            return false;
        }

        MessageSendConfig platformConfig = messagePlatformSendConfig.getPlatformConfig();
        MessageContentModel contentModel = new MessageContentModel(content, "text", null);
        MessageResp messageResp = messageFuture.sendMessage(platformConfig, contentModel, null);
        log.info("MessageSendService sendMessage result:{}", JSON.toJSONString(messageResp));
        return true;
    }
}
