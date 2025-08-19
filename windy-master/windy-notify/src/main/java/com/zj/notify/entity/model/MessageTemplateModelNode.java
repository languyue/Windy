
package com.zj.notify.entity.model;

import freemarker.template.Template;
import lombok.Data;

import javax.xml.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Violet
 * @describe xml模板模型
 * @since 2025/8/1 16:41
 */
@Data
@XmlRootElement(name = "templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class MessageTemplateModelNode {

    @XmlElement(name = "template")
    private List<TemplateNode> templates;


    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TemplateNode {

        @XmlAttribute
        private String id;

        @XmlAttribute
        private String name;

        @XmlElement(name = "success")
        private ContentConfigNode success;

        @XmlElement(name = "error")
        private ContentConfigNode error;

        @XmlElementWrapper(name = "channels")
        @XmlElement(name = "channel")
        private List<ChannelConfig> channels;

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class ContentConfigNode {

            @XmlAttribute
            private String type;

            @XmlValue
            private String template;

            @XmlTransient
            private Template compiledTemplate;
        }


        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class ChannelConfig {

            @XmlAttribute
            private String type;

            @XmlElement(name = "webhook")
            private String webhook;

            @XmlElement(name = "timeout")
            private Integer timeout;

            @XmlElement(name = "retry")
            private Integer retry;

            @XmlTransient // 关键修改：忽略此字段
            private Map<String, String> extraParams = new HashMap<>();
        }
    }
}
