package com.zj.client.handler.feature.executor;

import com.zj.contract.feature.exec.ExecutePointVo;
import com.zj.contract.event.payload.ExecuteRecordPayload;
import com.zj.contract.event.payload.FeatureHistoryPayload;
import com.zj.client.handler.feature.executor.invoker.strategy.ExecuteStrategyFactory;
import com.zj.client.handler.feature.executor.vo.FeatureExecuteContext;
import com.zj.contract.feature.exec.FeatureExecuteParam;
import com.zj.client.handler.notify.IResultEventNotify;
import com.zj.client.utils.ExceptionUtils;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.contract.event.ResultEvent;
import com.zj.contract.feature.FeatureResponse;
import com.zj.contract.enums.NotifyType;
import com.zj.contract.enums.ProcessStatus;
import com.zj.common.enums.TemplateType;
import com.zj.common.enums.TestStageType;
import com.zj.common.utils.IpUtils;
import com.zj.common.utils.OrikaUtil;
import com.zj.common.utils.TraceUtils;
import com.zj.plugin.loader.ExecuteDetailVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FeatureExecutorImpl implements IFeatureExecutor {

    private final ExecuteStrategyFactory executeStrategyFactory;

    private final UniqueIdService uniqueIdService;

    private final IResultEventNotify resultEventNotify;

    private final Executor featureExecutorPool;
    private final Executor cleanDirtyPool;

    public FeatureExecutorImpl(
            ExecuteStrategyFactory executeStrategyFactory, UniqueIdService uniqueIdService,
            IResultEventNotify resultEventNotify, @Qualifier("featureExecutePool") Executor featureExecutorPool,
            @Qualifier("cleanDirtyDataExecutePool") Executor cleanDirtyPool) {
        this.executeStrategyFactory = executeStrategyFactory;
        this.uniqueIdService = uniqueIdService;
        this.resultEventNotify = resultEventNotify;
        this.featureExecutorPool = featureExecutorPool;
        this.cleanDirtyPool = cleanDirtyPool;
    }

    @Override
    public void execute(FeatureExecuteParam featureParam) {
        String historyId = uniqueIdService.getUniqueId();
        FeatureHistoryPayload featureHistory = saveFeatureHistory(featureParam.getFeatureId(), historyId,
                featureParam.getTaskRecordId());

        CompletableFuture.runAsync(() -> {
            //1 根据用户的选择先排序执行点
            List<ExecutePointVo> executePoints = featureParam.getExecutePointList().stream()
                    .sorted(Comparator.comparing(ExecutePointVo::getSortOrder)).collect(Collectors.toList());

            AtomicInteger status = new AtomicInteger(ProcessStatus.SUCCESS.getType());
            FeatureExecuteContext featureExecuteContext = new FeatureExecuteContext();
            featureExecuteContext.bindMap(featureParam.getExecuteContext());
            featureExecuteContext.setHistoryId(historyId);
            Map<String, Object> globalContext = new HashMap<>();
            CountDownLatch countDownLatch = null;
            for (ExecutePointVo executePoint : executePoints) {
                TraceUtils.startNextSpan();
                waitIFExistAsyncPoint(countDownLatch);
                ExecuteRecordPayload executeRecord = new ExecuteRecordPayload();
                String recordId = uniqueIdService.getUniqueId();
                executeRecord.setExecuteRecordId(recordId);
                executeRecord.setHistoryId(historyId);
                try {
                    //2 使用策略类执行用例
                    featureExecuteContext.setRecordId(recordId);
                    featureExecuteContext.setLogId(featureParam.getLogId());

                    //如果是异步执行模版就需要添加countDown，保证后续任务是串行执行
                    if (Objects.equals(executePoint.getExecuteType(), TemplateType.THREAD.getType())) {
                        countDownLatch = new CountDownLatch(1);
                        featureExecuteContext.setCountDownLatch(countDownLatch);
                        status.set(ProcessStatus.RUNNING.getType());
                    }
                    List<FeatureResponse> responses = executeStrategyFactory.execute(executePoint, featureExecuteContext);
                    executeRecord.setStatus(judgeRecordStatus(responses));
                    executeRecord.setRecordResult(responses);

                    Map<String, Object> pointGlobalContext =
                            responses.stream().map(FeatureResponse::getContext).filter(Objects::nonNull)
                                    .flatMap(map -> map.entrySet().stream())
                                    .filter(entry -> Objects.nonNull(entry.getValue()))
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    globalContext.putAll(pointGlobalContext);
                } catch (Exception e) {
                    log.error("execute error", e);
                    FeatureResponse featureResponse = createFailResponse(executePoint, e);
                    executeRecord.setStatus(ProcessStatus.FAIL.getType());
                    executeRecord.setRecordResult(Collections.singletonList(featureResponse));
                } finally {
                    //3 保存执行点记录
                    saveRecord(featureParam, executePoint, executeRecord);
                    TraceUtils.removeTrace();
                }

                if (Objects.equals(executeRecord.getStatus(), ProcessStatus.FAIL.getType())) {
                    log.warn("execute feature error featureId= {}", executePoint.getFeatureId());
                    status.set(executeRecord.getStatus());

                    //只有非清理阶段的执行点执行失败才需要清理
                    if(!Objects.equals(executePoint.getTestStage(), TestStageType.CLEAN.getType())){
                        //如果用例执行异常，那么执行一下清理阶段的用例，减少测试环境的脏数据
                        cleanFeatureDirtyDataAsync(executePoints, featureExecuteContext);
                    }
                    break;
                }
            }

            //4 更新整个用例执行结果
            featureHistory.setExecuteStatus(status.get());
            ResultEvent resultEvent = new ResultEvent().executeId(featureParam.getTaskRecordId())
                    .notifyType(NotifyType.UPDATE_FEATURE_HISTORY)
                    .masterIP(featureParam.getMasterIp())
                    .logId(featureParam.getLogId())
                    .status(ProcessStatus.exchange(status.get()))
                    .context(globalContext)
                    .params(featureHistory);
            resultEventNotify.notifyEvent(resultEvent);
        }, featureExecutorPool);
    }

    /**
     * 异步执行清理动作
     */
    private void cleanFeatureDirtyDataAsync(List<ExecutePointVo> executePoints,
                                            FeatureExecuteContext featureExecuteContext) {
        List<ExecutePointVo> cleanPoints =
                executePoints.stream().filter(executePoint -> Objects.equals(executePoint.getTestStage(),
                        TestStageType.CLEAN.getType())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(cleanPoints)) {
            log.info("clean stage points is empty, not clean");
            return;
        }
        FeatureExecuteContext newContext = OrikaUtil.convert(featureExecuteContext, FeatureExecuteContext.class);
        log.info("start run clean stage points");
        CompletableFuture.runAsync(() -> cleanPoints.forEach(executePoint -> {
            try {
                log.info("start execute clean stage point={}", executePoint.getExecutorUnit().getName());
                executeStrategyFactory.execute(executePoint, newContext);
            } catch (Exception e) {
                log.debug("execute clean stage point error {}", e.getMessage());
            }
        }), cleanDirtyPool);
    }

    /**
     * 上个任务是异步任务的需要等上个任务执行之后再开始下一个避免异步任务错误后续任务的处理的结果
     */
    private void waitIFExistAsyncPoint(CountDownLatch countDownLatch) {
        if (Objects.isNull(countDownLatch) || countDownLatch.getCount() < 1) {
            return;
        }
        try {
            countDownLatch.await(5, TimeUnit.MINUTES);
        } catch (InterruptedException ignore) {
            log.info("async task not trigger,continue execute next task");
        }
    }

    public static Integer judgeRecordStatus(List<FeatureResponse> responses) {
        boolean processing = responses.stream().anyMatch(FeatureResponse::isProcessing);
        if (processing) {
            return ProcessStatus.RUNNING.getType();
        }
        boolean allSuccess = responses.stream().allMatch(FeatureResponse::isSuccess);
        return allSuccess ? ProcessStatus.SUCCESS.getType() : ProcessStatus.FAIL.getType();
    }

    private void saveRecord(FeatureExecuteParam featureParam, ExecutePointVo executePoint,
                            ExecuteRecordPayload executeRecord) {
        executeRecord.setExecutePointId(executePoint.getPointId());
        executeRecord.setExecutePointName(executePoint.getDescription());
        executeRecord.setExecuteType(executePoint.getExecuteType());
        executeRecord.setCreateTime(System.currentTimeMillis());
        executeRecord.setTestStage(executePoint.getTestStage());
        ResultEvent resultEvent = new ResultEvent().executeId(executeRecord.getExecuteRecordId())
                .notifyType(NotifyType.CREATE_EXECUTE_POINT_RECORD)
                .masterIP(featureParam.getMasterIp())
                .clientIp(IpUtils.getLocalIP())
                .logId(featureParam.getLogId())
                .status(ProcessStatus.exchange(executeRecord.getStatus())).params(executeRecord);
        resultEventNotify.notifyEvent(resultEvent);
    }

    public FeatureHistoryPayload saveFeatureHistory(String featureId, String historyId, String taskId) {
        FeatureHistoryPayload featureHistory = new FeatureHistoryPayload();
        featureHistory.setFeatureId(featureId);
        featureHistory.setExecuteStatus(ProcessStatus.RUNNING.getType());
        featureHistory.setHistoryId(historyId);
        featureHistory.setRecordId(taskId);
        featureHistory.setCreateTime(System.currentTimeMillis());

        ResultEvent resultEvent = new ResultEvent().executeId(historyId)
                .notifyType(NotifyType.CREATE_FEATURE_HISTORY)
                .status(ProcessStatus.RUNNING).params(featureHistory);
        resultEventNotify.notifyEvent(resultEvent);
        return featureHistory;
    }

    private FeatureResponse createFailResponse(ExecutePointVo executePoint, Exception e) {
        ExecuteDetailVo executeDetailVo = new ExecuteDetailVo();
        executeDetailVo.setErrorMessage(ExceptionUtils.getSimplifyError(e));
        executeDetailVo.setStatus(false);
        FeatureResponse featureResponse = new FeatureResponse();
        featureResponse.setPointId(executePoint.getPointId());
        featureResponse.setExecuteDetailVo(executeDetailVo);
        return featureResponse;
    }
}
