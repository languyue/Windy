package com.zj.notify.service.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import com.zj.common.utils.OkHttpUtil;
import com.zj.notify.starter.IMessageProvider;
import com.zj.notify.entity.bean.*;
import com.zj.notify.entity.enums.MessageChannelEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * @author Violet
 * @describe 企业微信消息提供者
 * @since 2025/7/20 00:28
 */
@Component
@Slf4j
public class WeChatMessageProvider implements IMessageProvider {

    @Override
    public String futureName() {
        return MessageChannelEnum.WE_TALE.getTag();
    }


    @Override
    public MessageResp sendMessage(MessageSendConfig messageSendConfig, MessageContentModel messageContent,
                                   MessageReceiveConfig messageReceiveConfig) {
        log.info("WeChatMessageProvider sendMessage sendConfig:{},msgContent:{}, receiveConfig:{}",
                JSON.toJSONString(messageSendConfig), messageContent, JSON.toJSONString(messageReceiveConfig));

        MessagePayload messagePayload = WeChatSendModelEnum.getSendModel(messageContent.getMessageType())
                .build(messageContent.getMessageContent(), messageReceiveConfig);

        String webhookUrl = messageSendConfig.getWebhook();
        try {
            OkHttpUtil.HttpResponse response = OkHttpUtil.doPostWithResponse(webhookUrl, messagePayload.build());
            return handleMessageSendResult(response);
        } catch (RestClientException e) {
            log.error("WeChatMessageProvider sendMessage error:", e);
            return MessageResp.fail(e.getMessage());
        }
    }

    @Override
    public void sendMessageSync(MessageSendConfig messageSendConfig, MessageContentModel messageContent,
                                MessageReceiveConfig messageReceiveConfig, BiConsumer<? super Void, ? super Throwable> callbackAction) {
        CompletableFuture.runAsync(() -> sendMessage(messageSendConfig, messageContent, messageReceiveConfig))
                .whenComplete(callbackAction)
                .exceptionally(ex -> {
                    log.error("WeChatMessageProvider sendMessage error:", ex);
                    return null;
                });
    }


    private MessageResp handleMessageSendResult(OkHttpUtil.HttpResponse response) {
        if (Objects.equals(response.getStatusCode(), HttpStatus.OK.value())) {
            return MessageResp.success(response.getBody());
        }
        return MessageResp.fail(response.getBody());
    }

    @Getter
    @AllArgsConstructor
    private enum WeChatSendModelEnum {

        TEXT("text") {
            @Override
            public MessagePayload build(String content, MessageReceiveConfig receiveConfig) {
                Map<String, Object> msgContent = new HashMap<>();
                msgContent.put("content", content);
                Set<String> uidSet = Optional.ofNullable(receiveConfig).map(MessageReceiveConfig::getUidList).orElse(Collections.emptySet());
                msgContent.put("mentioned_list", uidSet);
                return new MessagePayload(this.getTypeName(), msgContent);
            }
        },

        MARK_DOWN("markdown") {
            @Override
            public MessagePayload build(String content, MessageReceiveConfig receiveConfig) {
                HashMap<String, Object> markdown = new HashMap<>();
                markdown.put("content", content);
                return new MessagePayload(this.getTypeName(), markdown);
            }

        },

        IMAGE("image") {
            @Override
            public MessagePayload build(String content, MessageReceiveConfig receiveConfig) {
                Map<String, Object> imageContent = new HashMap<>();
                String base64 = JSON.parseObject(content).getString("base64");
                String md5 = JSON.parseObject(content).getString("md5");
                imageContent.put("base64", base64);
                imageContent.put("md5", md5);
                return new MessagePayload(this.getTypeName(), imageContent);
            }
        },

        NEWS("news") {
            @Override
            public MessagePayload build(String content, MessageReceiveConfig receiveConfig) {
                Map<String, Object> newsContent = new HashMap<>();

                List<Map<String, String>> articles = JSON.parseObject(content,
                        new TypeReference<List<Map<String, String>>>() {
                        });

                newsContent.put("articles", articles.stream()
                        .map(article -> {
                            Map<String, String> item = new HashMap<>();
                            item.put("title", article.get("title"));
                            item.put("description", article.get("description"));
                            item.put("url", article.get("url"));
                            item.put("picurl", article.get("picurl"));
                            return item;
                        })
                        .collect(Collectors.toList()));

                return new MessagePayload(this.getTypeName(), newsContent);
            }
        },

        FILE("file") {
            @Override
            public MessagePayload build(String content, MessageReceiveConfig receiveConfig) {
                Map<String, Object> fileContent = new HashMap<>();
                fileContent.put("media_id", JSON.parseObject(content).getString("media_id"));
                return new MessagePayload(this.getTypeName(), fileContent);
            }
        },

        TEMPLATE_CARD("template_card") {
            @Override
            public MessagePayload build(String content, MessageReceiveConfig receiveConfig) {
                // 模板卡片是最复杂的类型，直接透传JSON配置
                Map<String, Object> cardContent = JSON.parseObject(content, Map.class);
                return new MessagePayload(this.getTypeName(), cardContent);
            }
        };

        private final String typeName;

        public abstract MessagePayload build(String content, MessageReceiveConfig receiveConfig);

        public static WeChatSendModelEnum getSendModel(String type) {
            if (StringUtils.isBlank(type)) {
                return TEXT;
            }
            for (WeChatSendModelEnum value : WeChatSendModelEnum.values()) {
                if (Objects.equals(value.typeName, type)) {
                    return value;
                }
            }
            return TEXT;
        }
    }
}