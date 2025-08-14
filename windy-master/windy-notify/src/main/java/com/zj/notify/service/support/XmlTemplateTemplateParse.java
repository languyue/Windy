package com.zj.notify.service.support;


import com.zj.notify.starter.IMessageTemplateParse;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Violet
 * @describe 默认xml解析器
 * @since 2025/8/1 18:13
 */
@Slf4j
@Component
public class XmlTemplateTemplateParse implements IMessageTemplateParse {

    private static final Configuration freemarkerConfig;

    private final Map<String, Template> templateCache = new HashMap<>();

    static {
        freemarkerConfig = new Configuration(Configuration.VERSION_2_3_30);
        freemarkerConfig.setDefaultEncoding("UTF-8");
        freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }


    @Override
    public String parse(String templateContent, Map<String, Object> context) {
        // 根据templateContent获取到一个唯一值
        String uniqueId = String.valueOf(templateContent.hashCode());
        Template template = templateCache.get(uniqueId);
        if (template == null) {
            try {
                template = new Template(uniqueId, templateContent, freemarkerConfig);
                templateCache.put(uniqueId, template);
            } catch (IOException e) {
                log.error("XmlTemplateTemplateParse parse has error:", e);
                return null;
            }
        }

        String renderedContent = render(template, context);
        if (StringUtils.isBlank(renderedContent)) {
            log.error("XmlTemplateTemplateParse parse Failed to render template: {}", templateContent);
            return null;
        }

        // 5. 返回结果模型
        return renderedContent;
    }

    /**
     * 使用FreeMark渲染模板
     *
     * @param context 参数信息
     * @return 解析后的模板信息
     */
    public String render(Template template, Map<String, Object> context) {

        // 渲染模板
        try (StringWriter writer = new StringWriter()) {
            template.process(context, writer);
            return writer.toString();
        } catch (Exception e) {
            log.error("XmlTemplateTemplateParse Failed to render template: ", e);
            return null;
        }
    }
}
