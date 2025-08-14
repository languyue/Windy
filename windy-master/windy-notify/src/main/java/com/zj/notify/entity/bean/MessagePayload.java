package com.zj.notify.entity.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessagePayload {

    /**
     * 消息类型
     */
    @JSONField(name = "msgtype")
    private String msgType;

    /**
     * 消息发送内容
     */
    private Map<String, Object> content;

    public String build() {
        MessagePayload messagePayload = new MessagePayload();
        messagePayload.setMsgType(this.getMsgType());
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(messagePayload));
        jsonObject.put(this.msgType, this.content);
        return jsonObject.toJSONString();
    }
}