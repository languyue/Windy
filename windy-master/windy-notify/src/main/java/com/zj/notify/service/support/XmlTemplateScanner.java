package com.zj.notify.service.support;


import com.zj.notify.entity.bean.MessageSendConfig;
import com.zj.notify.entity.bean.MessageTemplatePayload;
import com.zj.notify.entity.enums.ContentNodeEnum;
import com.zj.notify.entity.model.MessageTemplateModelNode;
import com.zj.notify.starter.IMessageConfigManager;
import com.zj.notify.starter.IMessageTemplateManager;
import com.zj.notify.starter.ITemplateScanner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.util.*;


/**
 * @author Violet
 * @describe 默认模板扫描器，从resource目录扫描XML模板文件
 * @since 2025/7/20 01:08
 */
@Slf4j
@Component
public class XmlTemplateScanner implements ITemplateScanner, InitializingBean {


    private final Map<String, MessageTemplateModelNode.TemplateNode> templates = new HashMap<>();

    private static final String SCANNER_LOCATION = "classpath:template/*.xml";

    private boolean isScanned = false;

    private final IMessageTemplateManager messageTemplateManager;

    private final IMessageConfigManager messageConfigManager;

    public XmlTemplateScanner(IMessageTemplateManager messageTemplateManager, IMessageConfigManager messageConfigManager) {
        this.messageTemplateManager = messageTemplateManager;
        this.messageConfigManager = messageConfigManager;
    }


    @Override
    public void scannerTemplate() {
        if (isScanned) {
            return;
        }

        doScannerTemplate();
        doSendConfigRegister();
        doContentTemplateRegister();
        isScanned = true;
    }

    /**
     * 扫描模版
     */
    private void doScannerTemplate() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(SCANNER_LOCATION);

            JAXBContext context = JAXBContext.newInstance(MessageTemplateModelNode.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            // 找到所有的templateNode
            for (Resource resource : resources) {
                MessageTemplateModelNode templateRootNode = (MessageTemplateModelNode) unmarshaller.unmarshal(resource.getFile());
                templateRootNode.getTemplates().forEach(templateNode -> templates.put(templateNode.getId(), templateNode));
            }

            log.info("模板扫描完成，共加载 {} 个模板", templates.size());
        } catch (IOException | JAXBException e) {
            log.error("扫描模板文件失败", e);
        }
    }

    /**
     * 执行模板渠道发送配置注册到管理器中
     */
    private void doSendConfigRegister() {
        templates.forEach((templateId, templateNode) -> templateNode.getChannels().forEach(channel -> {
            MessageSendConfig messageSendConfig = new MessageSendConfig();
            BeanUtils.copyProperties(channel, messageSendConfig);
            messageConfigManager.registerMessageSendConfig(templateId, channel.getType(), messageSendConfig);
        }));
    }

    /**
     * 执行模板内容注册到管理器中
     */
    private void doContentTemplateRegister() {
        templates.forEach((templateId, templateNode) -> {
            MessageTemplateModelNode.TemplateNode.ContentConfigNode success = templateNode.getSuccess();
            MessageTemplateModelNode.TemplateNode.ContentConfigNode error = templateNode.getError();
            if (success != null) {
                MessageTemplatePayload templatePayload = new MessageTemplatePayload(success.getType(), success.getTemplate());
                messageTemplateManager.registerTemplate(templateId, ContentNodeEnum.SUCCESS.getNodeName(), templatePayload);
            }
            if (error != null) {
                MessageTemplatePayload templatePayload = new MessageTemplatePayload(error.getType(), error.getTemplate());
                messageTemplateManager.registerTemplate(templateId, ContentNodeEnum.ERROR.getNodeName(), templatePayload);
            }
        });
    }


    @Override
    public void afterPropertiesSet() {
        scannerTemplate();
    }
}
