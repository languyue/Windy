package com.zj.remote.invoker;

import com.zj.remote.discover.DiscoverService;
import com.zj.common.adapter.discover.ServiceInstance;
import com.zj.contract.port.IClientInvoker;
import com.zj.contract.feature.exec.FeatureExecuteParam;
import com.zj.contract.feature.exec.GenerateParam;
import com.zj.contract.pipeline.node.TaskNode;
import com.zj.contract.rpc.ClientCollectDto;
import com.zj.contract.rpc.ResponseMeta;
import com.zj.contract.dispatch.StopDispatch;
import com.zj.contract.service.ToolLoadResult;
import com.zj.contract.service.ToolVersionDto;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
public class EurekaClientInvokerAdapter extends BaseEurekaAdapter implements IClientInvoker {

    private static final String DISPATCH_GENERATE_TASK = "http://WindyClient/v1/client/dispatch/generate";
    private static final String LOAD_LANGUAGE_VERSION = "http://%s/v1/devops/client/load/build/version";
    private static final String DISPATCH_PIPELINE_TASK = "http://WindyClient/v1/client/dispatch/pipeline";
    private static final String DISPATCH_FEATURE_TASK = "http://WindyClient/v1/client/dispatch/feature";
    private static final String MASTER_NOTIFY_CLIENT_STOP = "http://%s/v1/client/task/stop";
    public static final String CLIENT_MONITOR_URL = "http://%s/v1/devops/client/instance";
    private static final ParameterizedTypeReference<ResponseMeta<ToolLoadResult>> META_TOOL_LOAD_RESULT =
            new ParameterizedTypeReference<ResponseMeta<ToolLoadResult>>() {};
    private static final ParameterizedTypeReference<ResponseMeta<ClientCollectDto>> META_CLIENT_COLLECT =
            new ParameterizedTypeReference<ResponseMeta<ClientCollectDto>>() {};

    private final DiscoverService discoverService;

    public EurekaClientInvokerAdapter(DiscoverService discoverService, RestTemplate restTemplate) {
        super(restTemplate);
        this.discoverService = discoverService;
    }

    @Override
    public boolean runGenerateTask(GenerateParam generateParam) {
        boolean result = postForSuccess(DISPATCH_GENERATE_TASK, generateParam);
        log.info("run generate response result ={}", result);
        return result;
    }

    @Override
    public boolean runPipelineTask(TaskNode pipelineTask, boolean isRequestSingle, String singleIp) {
        if (isRequestSingle) {
            Optional<ServiceInstance> optional =
                    discoverService.getServiceInstances(DiscoverService.WINDY_Client).stream()
                            .filter(service -> Objects.equals(service.getIp(), singleIp)).findAny();
            if (!optional.isPresent()) {
                log.info("send single request error, service not find ={}", singleIp);
                return false;
            }
            String url = DISPATCH_PIPELINE_TASK.replace(DiscoverService.WINDY_Client, optional.get().getHost());
            Response response = postWithIp(url, pipelineTask);
            if (Objects.nonNull(response)) {
                response.close();
            }
            return Optional.ofNullable(response).map(Response::isSuccessful).orElse(false);
        }
        boolean result = postForSuccess(DISPATCH_PIPELINE_TASK, pipelineTask);
        log.info("run pipeline response result ={}", result);
        return result;
    }

    @Override
    public boolean runFeatureTask(FeatureExecuteParam featureTask) {
        boolean result = postForSuccess(DISPATCH_FEATURE_TASK, featureTask);
        log.info("run feature response status result ={}", result);
        return result;
    }

    @Override
    public void stopTaskLoopQuery(StopDispatch stopDispatch) {
        CompletableFuture.runAsync(() -> {
            List<ServiceInstance> windyClientInstances = discoverService.getWindyClientInstances();
            windyClientInstances.forEach(serviceInstance -> {
                String url = String.format(MASTER_NOTIFY_CLIENT_STOP, serviceInstance.getHost());
                log.info("start stop loop query task status targetId={} targetType={}", stopDispatch.getTargetId(),
                        stopDispatch.getLogType());
                Response response = postWithIp(url, stopDispatch);
                if (Objects.nonNull(response)) {
                    response.close();
                }
            });
        });
    }

    @Override
    public List<ToolLoadResult> loadBuildTool(ToolVersionDto toolVersion) {
        List<ServiceInstance> serviceInstances = discoverService.getServiceInstances(DiscoverService.WINDY_Client);
        return serviceInstances.stream().map(service -> {
            ToolLoadResult toolLoadResult = new ToolLoadResult();
            toolLoadResult.setNodeIP(service.getIp());
            String url = String.format(LOAD_LANGUAGE_VERSION, service.getHost());
            Response response = postWithIp(url, toolVersion);
            if (Objects.isNull(response)) {
                toolLoadResult.setSuccess(false);
                return toolLoadResult;
            }
            try {
                ResponseMeta<ToolLoadResult> responseMeta = readEntity(response, META_TOOL_LOAD_RESULT);
                toolLoadResult.setSuccess(Optional.ofNullable(responseMeta).map(ResponseMeta::getData)
                        .map(ToolLoadResult::getSuccess).orElse(false));
            } catch (Exception e) {
                log.info("request client load tool error", e);
                toolLoadResult.setSuccess(false);
            } finally {
                response.close();
            }
            return toolLoadResult;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ClientCollectDto> requestClientMonitor() {
        List<ServiceInstance> serviceInstances = discoverService.getServiceInstances(DiscoverService.WINDY_Client);
        return serviceInstances.stream().map(service -> {
            String url = String.format(CLIENT_MONITOR_URL, service.getHost());
            log.info(" start request client monitor data url = {}", url);
            Response response = getWithIp(url);
            if (Objects.isNull(response)) {
                return null;
            }
            try {
                ResponseMeta<ClientCollectDto> result = readEntity(response, META_CLIENT_COLLECT);
                return Optional.ofNullable(result).map(ResponseMeta::getData).orElse(null);
            } catch (Exception e) {
                log.info("handle client monitor result error", e);
                return null;
            } finally {
                response.close();
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
