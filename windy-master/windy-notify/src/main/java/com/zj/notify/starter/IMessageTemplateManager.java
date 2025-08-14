package com.zj.notify.starter;


import com.zj.notify.entity.bean.MessageTemplatePayload;

/**
 * @author Violet
 * @describe 模版扫描器, 专门负责模板扫描
 * @since 2025/7/20 01:06
 */
public interface IMessageTemplateManager {

    /**
     * 注册模版
     *
     * @param templateId      模版ID
     * @param nodeType        内容Tag success or error
     * @param templatePayload 模版内容和模板类型
     */
    void registerTemplate(String templateId, String nodeType, MessageTemplatePayload templatePayload);

    /**
     * 获取模版内容
     * 模版内容Tag com.zj.notify.service.ITemplateScanner#getTemplateContent@Text
     *
     * @param templateId 模板id
     * @param nodeType   内容Tag
     * @return 模版内容
     */
    MessageTemplatePayload getTemplateContent(String templateId, String nodeType);
}
