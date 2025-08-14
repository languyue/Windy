package com.zj.notify.entity.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Violet
 * @describe XXXXXX
 * @since 2025/7/20 00:17
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MessageResp implements Serializable {

    private String code;

    private String msg;

    private String resId;

    public static MessageResp fail(String msg) {
        return new MessageResp("500", msg, null);
    }

    public static MessageResp success(String msg, String resId) {
        return new MessageResp("200", msg, resId);
    }

    public static MessageResp success(String msg) {
        return new MessageResp("200", msg, null);
    }
}
