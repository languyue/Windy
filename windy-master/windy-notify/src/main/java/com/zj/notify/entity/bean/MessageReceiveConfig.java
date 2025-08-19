package com.zj.notify.entity.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Violet
 * @describe 消息接收配置实体类
 * @link https://developer.work.weixin.qq.com/document/path/91770
 * @since 2025/7/20 01:06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageReceiveConfig {

    /**
     * userId的列表提醒群中的指定成员(@某个成员)，@all表示提醒所有人，如果开发者获取不到userid，可以使用mentioned_mobile_list
     */
    private Set<String> uidList;


    /**
     * 手机号列表，提醒手机号对应的群成员(@某个成员)，@all表示提醒所有人
     */
    private Set<String> phoneList;

    /**
     * 其他配置参数
     */
    @Builder.Default
    private Map<String, String> extraParams = new HashMap<>();
} 