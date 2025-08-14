package com.zj.notify.service.support;

import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.vo.NotifyConfigDto;
import com.zj.domain.repository.pipeline.ISystemConfigRepository;
import com.zj.notify.entity.bean.MessageSendConfig;
import com.zj.notify.starter.IMessageConfigManager;
import com.zj.notify.starter.IMessageConfigRegister;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;


/**
 * @author Violet
 * @describe 默认消息配置注册器实现
 * @since 2025/7/20 02:02
 */
@Slf4j
@Component
public class DefaultMessageConfigRegister implements IMessageConfigRegister, InitializingBean {

    private final IMessageConfigManager messageConfigManager;

    private final ISystemConfigRepository systemConfigRepository;

    public DefaultMessageConfigRegister(IMessageConfigManager messageConfigManager, ISystemConfigRepository systemConfigRepository) {
        this.messageConfigManager = messageConfigManager;
        this.systemConfigRepository = systemConfigRepository;
    }


    @Override
    public void messageChannelRegister() {
        NotifyConfigDto notifyConfig = systemConfigRepository.getNotifyConfig();
        if (notifyConfig == null || StringUtils.isBlank(notifyConfig.getPlatform())) {
            return;
        }
        MessageSendConfig sendConfig = OrikaUtil.convert(notifyConfig, MessageSendConfig.class);
        messageConfigManager.registerMessageSendConfig(null, notifyConfig.getPlatform(), sendConfig);
    }

    @Override
    public void afterPropertiesSet() {
        messageChannelRegister();
    }
}
