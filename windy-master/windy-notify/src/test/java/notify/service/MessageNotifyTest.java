package notify.service;

import com.zj.notify.service.IMessageSendService;
import lombok.extern.slf4j.Slf4j;
import notify.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;

/**
 * @author Violet
 * @describe 消息通知功能测试
 * @since 2025/7/20 01:06
 */
@Slf4j
public class MessageNotifyTest extends BaseTest {

    @Resource
    private IMessageSendService messageSendService;

    @Test
    public void testRegisterUser() {
        // 测试用户注册消息通知
        boolean result = messageSendService.sendMessageFromTemplate("com.zj.notify.entity.enums.MessageModuleType.DEPLOY", true, null);
        log.info("MessageNotifyTest testRegisterUser result:{}", result);
        Assert.assertTrue(result);
    }
} 