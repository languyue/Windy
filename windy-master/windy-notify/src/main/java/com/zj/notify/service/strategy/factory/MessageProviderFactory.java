package com.zj.notify.service.strategy.factory;

import com.zj.notify.starter.IMessageProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Violet
 * @describe XXXXXX
 * @since 2025/7/20 01:32
 */
@Component
@Slf4j
public class MessageProviderFactory {

    private static final Map<String, IMessageProvider> futureMap = new ConcurrentHashMap<>();
    
    public MessageProviderFactory(List<IMessageProvider> messageFutureList) {
        messageFutureList.forEach(item -> futureMap.put(item.futureName(), item));
    }

    public static IMessageProvider getMessageFuture(String key) {
        return futureMap.get(key);
    }
}
