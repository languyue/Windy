package com.zj.master.dispatch.task;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.zj.common.enums.DispatchType;
import com.zj.contract.enums.LogType;
import com.zj.contract.enums.ProcessStatus;
import com.zj.contract.feature.ExecutorUnit;
import com.zj.contract.feature.exec.ExecutePointVo;
import com.zj.contract.feature.exec.FeatureExecuteParam;
import com.zj.contract.json.ContractJson;
import com.zj.contract.port.IClientInvoker;
import com.zj.common.utils.IpUtils;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.feature.ExecutePointBO;
import com.zj.domain.entity.bo.feature.ExecuteTemplateBO;
import com.zj.domain.entity.bo.feature.FeatureHistoryBO;
import com.zj.domain.entity.bo.feature.FeatureInfoBO;
import com.zj.domain.entity.bo.feature.TaskRecordBO;
import com.zj.domain.entity.bo.feature.TestCaseConfigBO;
import com.zj.domain.repository.feature.ICaseGlobalContextCommandRepository;
import com.zj.domain.repository.feature.IExecutePointQueryRepository;
import com.zj.domain.repository.feature.IExecuteTemplateQueryRepository;
import com.zj.domain.repository.feature.IFeatureHistoryCommandRepository;
import com.zj.domain.repository.feature.IFeatureQueryRepository;
import com.zj.domain.repository.feature.ITaskRecordCommandRepository;
import com.zj.domain.repository.feature.ITestCaseConfigQueryRepository;
import com.zj.master.dispatch.feature.FeatureDispatch;
import com.zj.master.dispatch.listener.IStopEventListener;
import com.zj.master.dispatch.listener.InternalEvent;
import com.zj.master.dispatch.listener.InternalEventFactory;
import com.zj.master.entity.enums.EventType;
import com.zj.contract.feature.exec.ExecuteContext;
import com.zj.plugin.loader.ExecuteStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Slf4j
@Component
public class FeatureExecuteProxy implements IStopEventListener {

    public static final String TASK_FEATURE_TIPS = "no task need run";
    private final Map<String, FeatureTask> featureTaskMap = new ConcurrentHashMap<>();
    private final Executor executorService;
    private final TaskEndProcessor taskEndProcessor;
    private final IExecutePointQueryRepository executePointRepository;
    private final ITaskRecordCommandRepository taskRecordRepository;
    private final IFeatureHistoryCommandRepository featureHistoryRepository;
    private final IExecuteTemplateQueryRepository executeTemplateRepository;
    private final ITestCaseConfigQueryRepository caseConfigRepository;
    private final ICaseGlobalContextCommandRepository caseGlobalContextRepository;
    private final IFeatureQueryRepository featureRepository;
    private final IClientInvoker clientInvoker;

    public FeatureExecuteProxy(@Qualifier("featureExecutorPool") Executor executorService,
                               TaskEndProcessor taskEndProcessor,
                               IExecutePointQueryRepository executePointRepository,
                               ITaskRecordCommandRepository taskRecordRepository,
                               IFeatureHistoryCommandRepository featureHistoryRepository,
                               IExecuteTemplateQueryRepository executeTemplateRepository,
                               ITestCaseConfigQueryRepository caseConfigRepository,
                               ICaseGlobalContextCommandRepository caseGlobalContextRepository,
                               IFeatureQueryRepository featureRepository, IClientInvoker clientInvoker) {
        this.executorService = executorService;
        this.taskEndProcessor = taskEndProcessor;
        this.executePointRepository = executePointRepository;
        this.taskRecordRepository = taskRecordRepository;
        this.featureHistoryRepository = featureHistoryRepository;
        this.executeTemplateRepository = executeTemplateRepository;
        this.caseConfigRepository = caseConfigRepository;
        this.caseGlobalContextRepository = caseGlobalContextRepository;
        this.featureRepository = featureRepository;
        this.clientInvoker = clientInvoker;
    }

    public void execute(FeatureTask featureTask) {
        featureTaskMap.put(featureTask.getTaskRecordId(), featureTask);
        CompletableFuture.supplyAsync(() -> {
            LinkedBlockingQueue<String> featureIds = featureTask.getFeatureIds();
            String featureId = featureIds.poll();
            String taskRecordId = featureTask.getTaskRecordId();
            if (StringUtils.isBlank(featureId)) {
                featureTaskMap.remove(taskRecordId);
                return null;
            }

            TaskRecordBO taskRecord = taskRecordRepository.getTaskRecord(taskRecordId);
            if (!taskRecordId.startsWith(FeatureDispatch.TEMP_KEY) && (Objects.isNull(taskRecord)
                    || ProcessStatus.isCompleteStatus(taskRecord.getStatus()))) {
                if (Objects.isNull(taskRecord)) {
                    log.info("task record not found, not execute recordId={}", taskRecordId);
                } else {
                    log.info("task record is done not execute status={}", taskRecord.getStatus());
                }
                // 记录已完结或不存在时不会有后续回调清理，这里直接释放，避免 isExitTask 恒为 true
                featureTaskMap.remove(taskRecordId);
                return null;
            }

            FeatureExecuteParam featureExecuteParam = getFeatureExecuteParam(featureTask, featureId);
            featureExecuteParam.setDispatchType(DispatchType.FEATURE.name());
            featureExecuteParam.setMasterIp(IpUtils.getLocalIP());
            boolean result = clientInvoker.runFeatureTask(featureExecuteParam);
            log.info("run feature result = {}", result);
            //todo 执行用例失败的情况下需要修改用例的状态
            return featureId;
        }, executorService).whenComplete((featureId, e) -> {
            String recordId = Optional.ofNullable(featureId).orElse(TASK_FEATURE_TIPS);
            log.info("complete trigger action recordId = {}", recordId);
        }).exceptionally(e -> {
            log.error("handle task error", e);
            //todo 执行用例失败的情况下需要修改用例的状态
            return null;
        });
    }

    public boolean isExitTask(String recordId) {
        if (StringUtils.isBlank(recordId)) {
            return false;
        }
        return featureTaskMap.containsKey(recordId);
    }

    private FeatureExecuteParam getFeatureExecuteParam(FeatureTask featureTask, String featureId) {
        FeatureExecuteParam featureExecuteParam = new FeatureExecuteParam();
        featureExecuteParam.setFeatureId(featureId);
        featureExecuteParam.setExecuteContext(featureTask.getExecuteContext().toMap());
        featureExecuteParam.setTaskRecordId(featureTask.getTaskRecordId());
        List<ExecutePointVo> executePoints = executePointRepository.getExecutePointByFeatureId(featureId)
                .stream().map(this::toExecutePointVo).collect(Collectors.toList());
        featureExecuteParam.setExecutePointList(executePoints);
        return featureExecuteParam;
    }

    /**
     * 存储层的执行点只取执行任务需要的字段下发，同时把关联模版信息合并进执行点
     */
    private ExecutePointVo toExecutePointVo(ExecutePointBO executePoint) {
        ExecutePointVo pointVo = OrikaUtil.convert(executePoint, ExecutePointVo.class);
        pointVo.setExecutorUnit(wrapExecutorUnitTemplate(executePoint));
        return pointVo;
    }

    private ExecutorUnit wrapExecutorUnitTemplate(ExecutePointBO executePoint) {
        ExecutorUnit executorUnit = executePoint.getExecutorUnit();
        wrapSubExecutePoints(executorUnit);
        if (StringUtils.isBlank(executorUnit.getRelatedId())) {
            return executorUnit;
        }
        ExecuteTemplateBO executeTemplate = executeTemplateRepository.getExecuteTemplate(executorUnit.getRelatedId());
        if (Objects.isNull(executeTemplate)) {
            return executorUnit;
        }
        ExecutorUnit related = OrikaUtil.convert(executeTemplate, ExecutorUnit.class);
        related.setParams(executeTemplate.getParameterDefines());
        related.setHeaders(executeTemplate.getHeaders());
        executorUnit.setRelatedTemplate(related);
        return executorUnit;
    }

    /**
     * 针对if或者for循环的嵌套子执行点需要设置关联的模版
     */
    private void wrapSubExecutePoints(ExecutorUnit executorUnit) {
        if (Objects.isNull(executorUnit) || CollectionUtils.isEmpty(executorUnit.getExecutePoints())) {
            return;
        }
        executorUnit.getExecutePoints().forEach(point -> {
            String relatedId = point.getExecutorUnit().getRelatedId();
            if (StringUtils.isBlank(relatedId)) {
                return;
            }
            ExecuteTemplateBO executeTemplate = executeTemplateRepository.getExecuteTemplate(relatedId);
            if (Objects.isNull(executeTemplate)) {
                return;
            }
            ExecutorUnit related = OrikaUtil.convert(executeTemplate, ExecutorUnit.class);
            related.setParams(executeTemplate.getParameterDefines());
            related.setHeaders(executeTemplate.getHeaders());
            point.getExecutorUnit().setRelatedTemplate(related);
        });
    }

    public void featureStatusChange(String taskRecordId, FeatureHistoryBO history, Map<String, Object> context) {
        //优先替换全局上下文变量
        FeatureTask featureTask = featureTaskMap.get(taskRecordId);
        if (MapUtils.isNotEmpty(context)) {
            Optional.ofNullable(featureTask).ifPresent(task -> task.getExecuteContext().toMap().putAll(context));
            //异步将上下文替换
            InternalEventFactory.sendNotifyEvent(new InternalEvent(EventType.RECOVER_CONTEXT, history.getFeatureId(),
                    context));
        }

        if (Objects.isNull(featureTask)) {
            log.info("can not find task record, not cycle run");
            return;
        }
        boolean isTaskEnd = taskEndProcessor.process(taskRecordId, featureTask.getLogId());
        if (isTaskEnd) {
            log.info("task is end, not run again taskRecordId={}", taskRecordId);
            featureTaskMap.remove(taskRecordId);
            return;
        }

        log.info("feature task start cycle run");
        execute(featureTask);
    }

    @Subscribe
    @AllowConcurrentEvents
    public void recoverGlobalContext(InternalEvent event) {
        if (!Objects.equals(event.getEventType().getType(), EventType.RECOVER_CONTEXT.getType())) {
            return;
        }
        FeatureInfoBO feature = featureRepository.getFeatureById(event.getTargetId());
        if (Objects.isNull(feature)) {
            log.info("can not find feature, not recover global context ={}", event.getTargetId());
            return;
        }
        List<TestCaseConfigBO> caseConfigs = caseConfigRepository.getCaseConfigs(
                feature.getTestCaseId());
        //只回收那些在配置里声明过的全局参数，回收结果写运行态的 case_global_context，不回写配置表
        Set<String> declareKeys = caseConfigs.stream().map(TestCaseConfigBO::getParamKey).collect(Collectors.toSet());
        Map<String, String> recoverValues = event.getContext().entrySet().stream()
                .filter(entry -> declareKeys.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> String.valueOf(entry.getValue())));
        boolean result = caseGlobalContextRepository.recoverContext(feature.getTestCaseId(), recoverValues);
        log.info("batch recover global context result={} context={}", result, ContractJson.write(recoverValues));
    }

    @Override
    public LogType supportType() {
        return LogType.FEATURE_TASK;
    }

    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void stopEvent(InternalEvent event) {
        if (!Objects.equals(supportType(), event.getLogType())) {
            return;
        }

        String taskRecordId = event.getTargetId();
        FeatureTask featureTask = featureTaskMap.remove(taskRecordId);

        boolean updateStatus = taskRecordRepository.updateRecordStatus(taskRecordId, ProcessStatus.STOP.getType());
        boolean stopResult = featureHistoryRepository.stopTaskFeatures(taskRecordId, ProcessStatus.STOP);
        log.info("stop pipeline task taskId={} taskRecordId={} updateStatus={} stopResult={}",
                Optional.ofNullable(featureTask).map(FeatureTask::getTaskId).orElse(null), taskRecordId, updateStatus,
                stopResult);
    }

    public Integer getTaskSize() {
        return featureTaskMap.values().stream().mapToInt(task -> task.getFeatureIds().size()).sum();
    }
}
