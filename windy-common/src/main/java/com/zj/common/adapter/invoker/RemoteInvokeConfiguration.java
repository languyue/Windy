package com.zj.common.adapter.invoker;

import com.zj.common.adapter.invoker.eureka.EurekaClientInvokerAdapter;
import com.zj.common.adapter.invoker.eureka.EurekaConsoleInvokerAdapter;
import com.zj.common.adapter.invoker.eureka.EurekaMasterCommandAdapter;
import com.zj.common.adapter.discover.DiscoverService;
import com.zj.common.adapter.invoker.eureka.EurekaMasterInvokerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RemoteInvokeConfiguration {

    /**
     * master 的调用入口，同时提供 console 侧与 client 侧两个方向的接口
     */
    @Bean
    public EurekaMasterInvokerAdapter masterInvokerAdapter(RestTemplate restTemplate,
                                                           DiscoverService discoverService) {
        return new EurekaMasterInvokerAdapter(restTemplate, discoverService);
    }

    @Bean
    public IClientInvoker getClientInvoker(RestTemplate restTemplate, DiscoverService discoverService) {
        return new EurekaClientInvokerAdapter(discoverService, restTemplate);
    }

    /**
     * console 向 master 下发运行态命令的入口
     */
    @Bean
    public IMasterCommand getMasterCommand(RestTemplate restTemplate) {
        return new EurekaMasterCommandAdapter(restTemplate);
    }

    /**
     * master 向 console 投递领域事件的入口
     */
    @Bean
    public IConsoleInvoker getConsoleInvoker(RestTemplate restTemplate, DiscoverService discoverService) {
        return new EurekaConsoleInvokerAdapter(restTemplate, discoverService);
    }
}
