package com.zj.notify.rest;


import com.alibaba.fastjson.JSON;
import com.zj.common.entity.dto.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.notify.entity.vo.MessageNotifyParamDto;
import com.zj.notify.service.impl.MessageSendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/v1/devops/notify")
public class MessageNotifyRest {

    @Resource
    private MessageSendService messageSendService;

    @PostMapping("/message")
    public ResponseMeta<Boolean> sendMessage(@RequestBody String msgContent) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, messageSendService.sendMessage(msgContent));
    }

    @PostMapping("/message/template")
    public ResponseMeta<Boolean> sendMessageFromTemplate(@RequestBody MessageNotifyParamDto messageNotifyParamDto) {
        log.info("MessageNotifyRest sendMessageFromTemplate params:{}", JSON.toJSONString(messageNotifyParamDto));
        if (messageNotifyParamDto == null) {
            return new ResponseMeta<>(ErrorCode.ERROR);
        }

        boolean invokeResult = messageNotifyParamDto.isBindMethodInvokeResult();
        String contextParams = messageNotifyParamDto.getContextParams();
        return new ResponseMeta<>(ErrorCode.SUCCESS, messageSendService.sendMessageFromTemplate(
                messageNotifyParamDto.getTemplateId(), invokeResult, contextParams));
    }
}
