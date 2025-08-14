package com.zj.notify.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MessageModuleType {

    DEPLOY("部署", 1);

    private final String desc;

    private final Integer code;

    public static MessageModuleType getByCode(Integer messageTypeCode) {
        for (MessageModuleType value : values()) {
            if (value.code.equals(messageTypeCode)) {
                return value;
            }
        }
        return null;
    }
}
