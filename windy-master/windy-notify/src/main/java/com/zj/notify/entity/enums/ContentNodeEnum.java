package com.zj.notify.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ContentNodeEnum {

    SUCCESS("success"),

    ERROR("error");

    private final String nodeName;
}
