package com.zj.remote.invoker;

import com.zj.remote.discover.DiscoverService;
import com.zj.common.adapter.discover.ServiceInstance;
import com.zj.common.adapter.invoker.IConsoleInvoker;
import com.zj.common.entity.event.PipelineFinishedEvent;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

/**
 * console 事件通道的远端实现。
 *
 * console 注册名是 Windy，运行态进程用它来宣告执行结果，业务域自己决定后续变化。
 *
 * @author guyuelan
 */
@Slf4j
public class EurekaConsoleInvokerAdapter extends BaseEurekaAdapter implements IConsoleInvoker {

    private static final String PIPELINE_FINISHED = "http://%s/v1/devops/pipeline/finished";

    private final DiscoverService discoverService;

    public EurekaConsoleInvokerAdapter(RestTemplate restTemplate, DiscoverService discoverService) {
        super(restTemplate);
        this.discoverService = discoverService;
    }

    @Override
    public boolean notifyPipelineFinished(PipelineFinishedEvent event) {
        ServiceInstance console = discoverService.getWindyConsoleInstance();
        if (Objects.isNull(console)) {
            log.error("can not find console instance, drop pipeline finished event historyId={}",
                Objects.isNull(event) ? null : event.getHistoryId());
            return false;
        }

        String url = String.format(PIPELINE_FINISHED, console.getHost());
        log.info("notify console pipeline finished url={} historyId={}", url, event.getHistoryId());
        Response response = postWithIp(url, event);
        if (Objects.isNull(response)) {
            return false;
        }
        try {
            return response.isSuccessful();
        } finally {
            response.close();
        }
    }
}
