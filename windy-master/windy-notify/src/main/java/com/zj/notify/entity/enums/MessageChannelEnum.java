package com.zj.notify.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Violet
 * @describe XXXXXX
 * @since 2025/7/19 23:24
 */
@AllArgsConstructor
@Getter
public enum MessageChannelEnum {


    WE_TALE("企微", "we_chat");


    private final String desc;

    private final String tag;

}
