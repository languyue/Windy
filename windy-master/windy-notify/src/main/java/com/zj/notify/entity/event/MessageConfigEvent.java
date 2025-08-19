package com.zj.notify.entity.event;

import com.zj.notify.entity.bean.MessagePlatformConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@AllArgsConstructor
@Data
public class MessageConfigEvent {

    private  MessagePlatformConfig messagePlatformConfig;
}
