package com.zj.master.service;

import com.zj.contract.dispatch.DispatchResult;
import com.zj.contract.dispatch.DispatchTaskModel;
import com.zj.contract.dispatch.FeatureDispatchResult;
import com.zj.contract.dispatch.GenerateDispatchResult;
import com.zj.contract.dispatch.PipelineDispatchResult;
import com.zj.contract.dispatch.StopDispatch;
import com.zj.contract.dispatch.StopDispatchResult;
import com.zj.contract.enums.LogType;
import com.zj.contract.enums.ProcessStatus;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.contract.json.ContractJson;
import com.zj.common.utils.IpUtils;
import com.zj.domain.entity.bo.log.DispatchLogBO;
import com.zj.domain.repository.log.IDispatchLogCommandRepository;
import com.zj.master.dispatch.Dispatcher;
import com.zj.master.dispatch.listener.IStopEventListener;
import com.zj.master.dispatch.listener.InternalEvent;
import com.zj.master.dispatch.listener.InternalEventFactory;
import com.zj.master.entity.enums.EventType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author guyuelan
 * @since 2023/5/12
 */
@Slf4j
@Service
public class TaskLogService {

  private final UniqueIdService uniqueIdService;
  private final IDispatchLogCommandRepository taskLogRepository;
  private final Dispatcher dispatcher;
  private final List<IStopEventListener> stopListeners;

  public TaskLogService(UniqueIdService uniqueIdService, IDispatchLogCommandRepository taskLogRepository,
      Dispatcher dispatcher, List<IStopEventListener> stopListeners) {
    this.uniqueIdService = uniqueIdService;
    this.taskLogRepository = taskLogRepository;
    this.dispatcher = dispatcher;
    this.stopListeners = stopListeners;
  }

  public DispatchResult createTask(DispatchTaskModel task) {
    log.info("receive task detail ={}", ContractJson.write(task));
    return doCreateTask(task);
  }

  /**
   * 用例任务与批量执行用例共用同一入口，两种流程只能由请求体的type决定，
   * 必须在选定executor之前校验，否则会用错流程跑起来
   */
  public FeatureDispatchResult createFeatureTask(DispatchTaskModel task) {
    LogType logType = LogType.exchange(task.getType());
    if (!Objects.equals(LogType.FEATURE, logType) && !Objects.equals(LogType.FEATURE_TASK, logType)) {
      log.info("feature task type invalid type={}", task.getType());
      return FeatureDispatchResult.rejected("任务类型必须是FEATURE或FEATURE_TASK");
    }
    log.info("receive feature task detail ={}", ContractJson.write(task));
    return toFeatureResult(doCreateTask(task));
  }

  /**
   * 流水线任务的类型由入口决定，请求体中的type不参与判断
   */
  public PipelineDispatchResult createPipelineTask(DispatchTaskModel task) {
    task.setType(LogType.PIPELINE.getType());
    log.info("receive pipeline task detail ={}", ContractJson.write(task));
    return toPipelineResult(doCreateTask(task));
  }

  /**
   * 生成二方包任务的类型由入口决定，请求体中的type不参与判断
   */
  public GenerateDispatchResult createGenerateTask(DispatchTaskModel task) {
    task.setType(LogType.GENERATE.getType());
    log.info("receive generate task detail ={}", ContractJson.write(task));
    return toGenerateResult(doCreateTask(task));
  }

  private DispatchResult doCreateTask(DispatchTaskModel task) {
    DispatchLogBO taskLog = saveLog(task);
    DispatchResult dispatchResult = dispatcher.dispatch(task, taskLog.getLogId());
    if (Objects.isNull(dispatchResult) || !dispatchResult.accepted()) {
      //被拒绝的任务不会有任何回调收尾，日志必须直接置为失败，不能停在RUNNING
      log.info("dispatch task rejected logId={} message={}", taskLog.getLogId(),
          Objects.isNull(dispatchResult) ? null : dispatchResult.getMessage());
      taskLogRepository.updateLogStatus(taskLog.getLogId(), ProcessStatus.FAIL.getType());
    }
    return dispatchResult;
  }

  private FeatureDispatchResult toFeatureResult(DispatchResult result) {
    if (result instanceof FeatureDispatchResult) {
      return (FeatureDispatchResult) result;
    }
    return FeatureDispatchResult.rejected(resultMessage(result));
  }

  private PipelineDispatchResult toPipelineResult(DispatchResult result) {
    if (result instanceof PipelineDispatchResult) {
      return (PipelineDispatchResult) result;
    }
    return PipelineDispatchResult.rejected(resultMessage(result));
  }

  private GenerateDispatchResult toGenerateResult(DispatchResult result) {
    if (result instanceof GenerateDispatchResult) {
      return (GenerateDispatchResult) result;
    }
    return GenerateDispatchResult.rejected(resultMessage(result));
  }

  private String resultMessage(DispatchResult result) {
    return Objects.isNull(result) ? "调度执行器未返回结果" : result.getMessage();
  }

  private DispatchLogBO saveLog(DispatchTaskModel task) {
    DispatchLogBO taskLog = new DispatchLogBO();
    taskLog.setLogId(uniqueIdService.getUniqueId());
    taskLog.setSourceIds(task.getSourceIds());
    taskLog.setSourceName(task.getSourceName());
    taskLog.setLogStatus(ProcessStatus.RUNNING.getType());
    taskLog.setLogType(task.getType());
    taskLog.setNodeIp(IpUtils.getLocalIP());
    taskLog.setLockVersion(1);
    taskLogRepository.saveLog(taskLog);
    return taskLog;
  }

  public StopDispatchResult pauseTask(StopDispatch stopDispatch) {
    if (Objects.isNull(stopDispatch) || StringUtils.isBlank(stopDispatch.getTargetId())) {
      return StopDispatchResult.rejected("停止任务缺少执行记录Id");
    }

    LogType logType = stopDispatch.getLogType();
    if (Objects.isNull(logType)) {
      return StopDispatchResult.rejected("停止任务缺少任务类型");
    }

    boolean supportStop = stopListeners.stream()
        .anyMatch(listener -> Objects.equals(listener.supportType(), logType));
    if (!supportStop) {
      log.info("task type not support stop type={} targetId={}", logType, stopDispatch.getTargetId());
      return StopDispatchResult.rejected("任务类型[" + logType.getDesc() + "]不支持停止");
    }

    InternalEvent event = new InternalEvent();
    event.setEventType(EventType.STOP);
    event.setLogType(logType);
    event.setTargetId(stopDispatch.getTargetId());
    InternalEventFactory.sendNotifyEvent(event);
    return StopDispatchResult.accepted("停止指令已下发");
  }
}
