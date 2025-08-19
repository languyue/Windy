package com.zj.notify.entity.bean;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MessagePlatformConfig {

    private String platform;

    private MessageSendConfig platformConfig;
}
