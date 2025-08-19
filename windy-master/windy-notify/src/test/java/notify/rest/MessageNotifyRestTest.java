package notify.rest;

import com.alibaba.fastjson.JSON;
import com.zj.common.entity.dto.ResponseMeta;
import com.zj.notify.entity.vo.MessageNotifyParamDto;
import com.zj.notify.rest.MessageNotifyRest;
import lombok.extern.slf4j.Slf4j;
import notify.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.HashMap;

@Slf4j
public class MessageNotifyRestTest extends BaseTest {

    @Resource
    private MessageNotifyRest messageNotifyRest;

    @Test
    public void sendMessageTest() {
        ResponseMeta<Boolean> sendResult = messageNotifyRest.sendMessage("123465757575");
        log.info("MessageNotifyRest sendMessage params:{}", JSON.toJSONString(sendResult));
        Assert.assertNull(sendResult);
    }


    /**
     *  ▸ **项目名称**: ${deploy.projectName}
     *  ▸ **环境**: ${deploy.env}
     *  ▸ **耗时**: ${deploy.duration}秒
     *  ▸ **状态**: <font color="green">成功</font>
     *  ▸ **访问地址**: [${deploy.domain}](${deploy.domainUrl})
     *  ▸ **健康检查**: [${deploy.healthCheckUrl}](${deploy.healthCheckUrl})
     *  ▸ **部署日志**:
     *  ```bash
     *  ${deploy.last10LinesOfLog}
     */
    @Test
    public void sendMessageFromTemplateTest() {
        String templateId = "com.zj.notify.entity.enums.MessageModuleType.DEPLOY";
        HashMap<String, String> paramsMap = new HashMap<>();

        paramsMap.put("projectName", "测试项目");
        paramsMap.put("env", "prod");
        paramsMap.put("duration", "103");
        paramsMap.put("domain", "www.baidu.com");
        paramsMap.put("domainUrl", "112121");
        paramsMap.put("healthCheckUrl", "122222");
        paramsMap.put("last10LinesOfLog", "deployLog");

        MessageNotifyParamDto messageNotifyParamDto = new MessageNotifyParamDto(templateId, true, JSON.toJSONString(paramsMap));
        ResponseMeta<Boolean> sendResult = messageNotifyRest.sendMessageFromTemplate(messageNotifyParamDto);
        log.info("MessageNotifyRest sendMessageFromTemplateTest params:{}", JSON.toJSONString(sendResult));
        Assert.assertNull(sendResult);
    }
}
