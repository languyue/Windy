package com.zj.notify.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class MessageNotifyParamDto implements Serializable {

    private String templateId;

    private boolean bindMethodInvokeResult;

    private String contextParams;
}
