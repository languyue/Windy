package com.zj.notify.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SpringSpiFactory implements ApplicationContextAware  {

    private static ConfigurableListableBeanFactory beanFactory;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        beanFactory = (ConfigurableListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        log.info("BeanFactory initialized successfully");
    }


    public static <T> T load(Class<T> clazz, T defaultInstance) {
        // 2. 尝试Spring SPI
        try {
            List<T> instances = SpringFactoriesLoader.loadFactories(clazz, SpringSpiFactory.class.getClassLoader());
            if (!instances.isEmpty()) {
                registerBean(clazz, instances.get(0));
                return instances.get(0); // 返回第一个实例
            }
        } catch (Exception e) {
            log.warn("Failed to load from Spring SPI for class: {}", clazz.getName(), e);
        }

        // 3. SPI没有则尝试Spring容器
        if (beanFactory != null) {
            try {
                T bean = beanFactory.getBean(clazz);
                log.debug("Found bean from Spring context for class: {}", clazz.getName());
                return bean;
            } catch (Exception ignored) {
                log.warn("SpringSpiFactory get 找不到bean: {}", clazz.getName());
            }
        }

        // 4. 都没有则使用默认实现
        log.debug("Using default instance for class: {}", clazz.getName());
        return defaultInstance;
    }


    /**
     * 动态注册Bean到Spring容器
     */
    private static <T> void registerBean(Class<T> spiInterface, T instance) {
        // 直接注册单例实例（Spring 4.x 兼容）
        beanFactory.registerSingleton(spiInterface.getSimpleName() + "_" + instance.getClass().getSimpleName(), instance);
        log.info("Registered SPI implementation as Spring bean: {} -> {}", spiInterface.getName(), instance.getClass().getName());
    }

}
