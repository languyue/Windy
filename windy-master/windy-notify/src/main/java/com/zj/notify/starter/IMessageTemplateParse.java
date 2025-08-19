package com.zj.notify.starter;


import java.util.Map;


/**
 * @author Violet
 * @describe 消息模板解析器, 用于模版参数的填充，解析真正的消息内容
 * @since 2025/7/19 23:54
 */
public interface IMessageTemplateParse {

    /**
     * 模版参数解析
     *
     * @param templateContent 模版内容
     * @param contextParams   方法上下文参数
     * @return 解析后的消息体
     */
    String parse(String templateContent, Map<String, Object> contextParams);
}
