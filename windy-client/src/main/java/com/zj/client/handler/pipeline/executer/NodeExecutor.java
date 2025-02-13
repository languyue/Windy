package com.zj.client.handler.pipeline.executer;

import com.zj.client.handler.notify.IResultEventNotify;
import com.zj.client.handler.pipeline.executer.intercept.INodeExecuteInterceptor;
import com.zj.client.handler.pipeline.executer.notify.PipelineEventFactory;
import com.zj.client.handler.pipeline.executer.trigger.INodeTrigger;
import com.zj.client.handler.pipeline.executer.vo.PipelineStatusEvent;
import com.zj.client.handler.pipeline.executer.vo.TaskNode;
import com.zj.client.handler.pipeline.executer.vo.TaskNodeRecord;
import com.zj.client.handler.pipeline.executer.vo.TriggerContext;
import com.zj.client.utils.ExceptionUtils;
import com.zj.common.enums.NotifyType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.exception.ErrorCode;
import com.zj.common.exception.ExecuteException;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.common.entity.dto.ResultEvent;
import com.zj.common.utils.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/3/30
 */
@Slf4j
@Component
public class NodeExecutor {

  public static final String TRIGGER_TASK_ERROR = "trigger task error";
  private final List<INodeExecuteInterceptor> interceptors;
  private final Map<String, INodeTrigger> triggerMap;
  private final UniqueIdService uniqueIdService;
  private final IResultEventNotify resultEventNotify;

  public NodeExecutor(List<INodeExecuteInterceptor> interceptors, List<INodeTrigger> invokers,
      UniqueIdService uniqueIdService, IResultEventNotify resultEventNotify) {
    this.interceptors = interceptors;
    triggerMap = invokers.stream()
        .collect(Collectors.toMap(invoker -> invoker.type().name(), invoker -> invoker));
    this.uniqueIdService = uniqueIdService;
    this.resultEventNotify = resultEventNotify;
  }

  /**
   * 单个节点的执行逻辑主要涉及记录状态、调用第三方接口、查询任务状态（拦截器中实现）
   */
  public void runNodeTask(String historyId, TaskNode node) {
    log.info("start run task historyId={}", historyId);
    AtomicReference<ProcessStatus> statusAtomic = new AtomicReference<>(ProcessStatus.RUNNING);
    List<String> errorMsg = Collections.singletonList(TRIGGER_TASK_ERROR);
    String recordId = uniqueIdService.getUniqueId();
    try {
      node.setRecordId(recordId);
      interceptors.forEach(interceptor -> interceptor.before(node));

      saveNodeRecord(historyId, node, recordId, statusAtomic);

      INodeTrigger nodeTrigger = triggerMap.get(node.getExecuteType());
      if (Objects.isNull(nodeTrigger)) {
        throw new ExecuteException(ErrorCode.UNKNOWN_EXECUTE_TYPE);
      }

      Object context = node.getRequestContext();
      TriggerContext triggerContext = new TriggerContext(context);
      nodeTrigger.triggerRun(triggerContext, node);
    } catch (ExecuteException executeException) {
      statusAtomic.set(ProcessStatus.FAIL);
      errorMsg = Collections.singletonList(executeException.getMessage());
    } catch (Exception e) {
      log.error("execute pipeline node error recordId={}", recordId, e);
      //如果请求失败则直接流水线终止
      statusAtomic.set(ProcessStatus.FAIL);
      errorMsg = ExceptionUtils.getErrorMsg(e);
    }

    notifyNodeEvent(node, statusAtomic.get(), errorMsg);

    interceptors.forEach(interceptor -> interceptor.after(node, statusAtomic.get()));
  }

  private void saveNodeRecord(String historyId, TaskNode node, String recordId,
      AtomicReference<ProcessStatus> statusAtomic) {
    long currentTimeMillis = System.currentTimeMillis();
    TaskNodeRecord taskNodeRecord = TaskNodeRecord.builder().historyId(historyId).recordId(recordId)
        .status(statusAtomic.get().getType()).nodeId(node.getNodeId()).createTime(currentTimeMillis)
        .updateTime(currentTimeMillis).build();

    ResultEvent resultEvent = new ResultEvent().executeId(recordId)
        .notifyType(NotifyType.CREATE_NODE_RECORD)
        .status(ProcessStatus.RUNNING)
        .params(taskNodeRecord)
        .masterIP(node.getMasterIp())
        .clientIp(IpUtils.getLocalIP())
        .logId(node.getLogId());
    resultEventNotify.notifyEvent(resultEvent);
  }


  private void notifyNodeEvent(TaskNode node, ProcessStatus status, List<String> errorMsg) {
    log.info("shutdown pipeline recordId={}", node.getRecordId());
    if (!status.isFailStatus()) {
      return;
    }
    PipelineStatusEvent event = PipelineStatusEvent.builder().taskNode(node).processStatus(status)
        .errorMsg(errorMsg).build();
    PipelineEventFactory.sendNotifyEvent(event);
  }
}
