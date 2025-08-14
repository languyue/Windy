package com.zj.notify.service.support;

import com.zj.notify.entity.bean.MessageTemplatePayload;
import com.zj.notify.starter.IMessageTemplateManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class MessageTemplateManager implements IMessageTemplateManager {

    private final Map<String, MessageTemplatePayload> templatePayloadMap = new HashMap<>();


    @Override
    public void registerTemplate(String templateId, String nodeType, MessageTemplatePayload templatePayload) {
        if (StringUtils.isAnyBlank(templateId, nodeType) || templatePayload == null) {
            return;
        }

        String templateContentTag = templateId + "#" + nodeType;
        if (templatePayloadMap.containsKey(templateContentTag)) {
            return;
        }

        templatePayloadMap.put(templateContentTag, templatePayload);
    }

    @Override
    public MessageTemplatePayload getTemplateContent(String templateId, String nodeType) {
        String templateContentTag = templateId + "#" + nodeType;
        return templatePayloadMap.get(templateContentTag);
    }
}
