package com.zj.notify.entity.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class MessageTemplatePayload {

    private String templateType;

    private String templateContent;
}
