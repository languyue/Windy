package com.zj.remote.invoker;

import com.zj.contract.port.IMasterApi;
import com.zj.contract.port.IMasterCallback;
import com.zj.contract.dispatch.DispatchResult;
import com.zj.contract.dispatch.DispatchTaskModel;
import com.zj.contract.dispatch.FeatureDispatchResult;
import com.zj.contract.dispatch.GenerateDispatchResult;
import com.zj.contract.dispatch.PipelineDispatchResult;
import com.zj.contract.dispatch.StopDispatch;
import com.zj.contract.dispatch.StopDispatchResult;
import com.zj.contract.rpc.MasterCollectDto;
import com.zj.contract.rpc.NodeRecordStatus;
import com.zj.contract.rpc.PluginInfo;
import com.zj.contract.rpc.ResponseMeta;
import com.zj.contract.rpc.ResponseStatusModel;
import com.zj.contract.event.ResultEvent;
import com.zj.remote.discover.DiscoverService;
import com.zj.common.adapter.discover.ServiceInstance;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class EurekaMasterInvokerAdapter extends BaseEurekaAdapter implements IMasterApi,
        IMasterCallback {
    private static final String DISPATCH_FEATURE_TASK = "http://WindyMaster/v1/devops/dispatch/task/feature";
    private static final String DISPATCH_PIPELINE_TASK = "http://WindyMaster/v1/devops/dispatch/task/pipeline";
    private static final String DISPATCH_GENERATE_TASK = "http://WindyMaster/v1/devops/dispatch/task/generate";
    private static final String NOTIFY_TASK_RESULT = "http://WindyMaster/v1/devops/dispatch/notify";
    private static final String QUERY_NODE_RECORD = "http://WindyMaster/v1/devops/master/record/%s";
    private static final ParameterizedTypeReference<ResponseMeta<NodeRecordStatus>> META_NODE_RECORD_STATUS =
            new ParameterizedTypeReference<ResponseMeta<NodeRecordStatus>>() {};
    private static final ParameterizedTypeReference<ResponseMeta<FeatureDispatchResult>> META_FEATURE_RESULT =
            new ParameterizedTypeReference<ResponseMeta<FeatureDispatchResult>>() {};
    private static final ParameterizedTypeReference<ResponseMeta<PipelineDispatchResult>> META_PIPELINE_RESULT =
            new ParameterizedTypeReference<ResponseMeta<PipelineDispatchResult>>() {};
    private static final ParameterizedTypeReference<ResponseMeta<GenerateDispatchResult>> META_GENERATE_RESULT =
            new ParameterizedTypeReference<ResponseMeta<GenerateDispatchResult>>() {};
    private static final ParameterizedTypeReference<ResponseMeta<StopDispatchResult>> META_STOP_RESULT =
            new ParameterizedTypeReference<ResponseMeta<StopDispatchResult>>() {};
    private static final ParameterizedTypeReference<ResponseStatusModel> TASK_STATUS =
            new ParameterizedTypeReference<ResponseStatusModel>() {};
    private static final ParameterizedTypeReference<ResponseMeta<List<PluginInfo>>> META_PLUGIN_LIST =
            new ParameterizedTypeReference<ResponseMeta<List<PluginInfo>>>() {};
    private static final ParameterizedTypeReference<ResponseMeta<MasterCollectDto>> META_MASTER_COLLECT =
            new ParameterizedTypeReference<ResponseMeta<MasterCollectDto>>() {};
    public static final String STOP_DISPATCH_TASK = "http://WindyMaster/v1/devops/dispatch/stop";
    public static final String MASTER_MONITOR_URL = "http://%s/v1/devops/master/instance";
    public static final String GET_PLUGIN_LIST = "http://WindyMaster/v1/devops/master/plugins";
    public static final String GET_FEATURE_TASK_STATUS = "http://WindyMaster/v1/devops/master/task/%s/status";
    private final DiscoverService discoverService;

    public EurekaMasterInvokerAdapter(RestTemplate restTemplate, DiscoverService discoverService) {
        super(restTemplate);
        this.discoverService = discoverService;
    }

    @Override
    public FeatureDispatchResult runFeatureTask(DispatchTaskModel dispatchTaskModel) {
        return postDispatchResult(DISPATCH_FEATURE_TASK, dispatchTaskModel, META_FEATURE_RESULT,
                FeatureDispatchResult::rejected);
    }

    @Override
    public GenerateDispatchResult runGenerateTask(DispatchTaskModel dispatchTaskModel) {
        return postDispatchResult(DISPATCH_GENERATE_TASK, dispatchTaskModel, META_GENERATE_RESULT,
                GenerateDispatchResult::rejected);
    }

    @Override
    public PipelineDispatchResult startPipelineTask(DispatchTaskModel dispatchTaskModel) {
        return postDispatchResult(DISPATCH_PIPELINE_TASK, dispatchTaskModel, META_PIPELINE_RESULT,
                PipelineDispatchResult::rejected);
    }

    @Override
    public ResponseStatusModel getFeatureTaskStatus(String taskRecordId) {
        String url = String.format(GET_FEATURE_TASK_STATUS, taskRecordId);
        return get(url, TASK_STATUS);
    }

    @Override
    public boolean notifyExecuteEvent(ResultEvent resultEvent) {
        List<ServiceInstance> serviceInstances = discoverService.getServiceInstances(DiscoverService.WINDY_MASTER);
        boolean notifySuccess = serviceInstances.stream()
                .filter(serviceInstance -> Objects.equals(serviceInstance.getHost(), resultEvent.getMasterIP()))
                .findFirst().map(serviceInstance -> {
                    String url = NOTIFY_TASK_RESULT.replace(DiscoverService.WINDY_MASTER, serviceInstance.getHost());
                    log.info("start notify master ip={} result", serviceInstance.getHost());
                    // 如果触发任务执行的master节点存在那么优先访问触发任务的master节点
                    Response response = postWithIp(url, resultEvent);
                    if (Objects.nonNull(response)){
                        response.close();
                        return response.isSuccessful();
                    }
                    return false;
                }).orElse(false);
        if (notifySuccess) {
            return true;
        }

        //master节点不可达时，尝试使用其他的master节点
        boolean fallbackSuccess = postForSuccess(NOTIFY_TASK_RESULT, resultEvent);
        log.info("notify event fallback result={}", fallbackSuccess);
        return fallbackSuccess;
    }

    @Override
    public NodeRecordStatus getNodeRecordStatus(String recordId) {
        String url = String.format(QUERY_NODE_RECORD, recordId);
        ResponseMeta<NodeRecordStatus> responseMeta = exchange(url, HttpMethod.GET, null, META_NODE_RECORD_STATUS);
        return Optional.ofNullable(responseMeta).map(ResponseMeta::getData).orElse(null);
    }

    @Override
    public List<PluginInfo> getAvailablePlugins() {
        ResponseMeta<List<PluginInfo>> responseMeta = get(GET_PLUGIN_LIST, META_PLUGIN_LIST);
        return Optional.ofNullable(responseMeta).map(ResponseMeta::getData).orElseGet(ArrayList::new);
    }

    @Override
    public StopDispatchResult stopDispatch(StopDispatch stopDispatch) {
        StopDispatchResult result = postDispatchResult(STOP_DISPATCH_TASK, stopDispatch, META_STOP_RESULT,
                StopDispatchResult::rejected);
        log.info("stop dispatch task status={} message={}", result.getStatus(), result.getMessage());
        return result;
    }

    /**
     * 调度结果统一解析：master不可达或报文异常时返回拒绝结果，调用方总能拿到状态与描述
     */
    private <T extends DispatchResult> T postDispatchResult(String url, Object body,
            ParameterizedTypeReference<ResponseMeta<T>> type, Function<String, T> rejectedFactory) {
        ResponseMeta<T> responseMeta = post(url, body, type);
        if (Objects.nonNull(responseMeta) && Objects.nonNull(responseMeta.getData())) {
            return responseMeta.getData();
        }

        String message = Objects.isNull(responseMeta) || !StringUtils.hasText(responseMeta.getMessage())
                ? "master节点不可达" : responseMeta.getMessage();
        log.info("dispatch task request failed url={} message={}", url, message);
        return rejectedFactory.apply(message);
    }

    @Override
    public List<MasterCollectDto> requestMasterMonitor() {
        List<ServiceInstance> serviceInstances = discoverService.getServiceInstances(DiscoverService.WINDY_MASTER);
        return serviceInstances.stream().map(service -> {
            String url = String.format(MASTER_MONITOR_URL, service.getHost());
            Response response = getWithIp(url);
            if (Objects.isNull(response)) {
                return null;
            }
            try {
                ResponseMeta<MasterCollectDto> result = readEntity(response, META_MASTER_COLLECT);
                return Optional.ofNullable(result).map(ResponseMeta::getData).orElse(null);
            } catch (Exception e) {
                log.info("handle master monitor error", e);
                return null;
            } finally {
                response.close();
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
