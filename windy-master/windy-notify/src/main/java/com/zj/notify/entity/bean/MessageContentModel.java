package com.zj.notify.entity.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Violet
 * @describe XXXXXX
 * @since 2025/8/1 12:02
 */
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MessageContentModel {

    /**
     * 消息内容
     */
    private String messageContent;

    /**
     * 消息发送类型
     */
    private String messageType;

    /**
     * 其他配置参数
     */
    private Map<String, String> extraParams = new HashMap<>();
}
