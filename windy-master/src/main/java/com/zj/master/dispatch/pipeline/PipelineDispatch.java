package com.zj.master.dispatch.pipeline;

import com.zj.contract.pipeline.ConfigDetail;
import com.zj.contract.enums.LogType;
import com.zj.contract.enums.ProcessStatus;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.contract.dispatch.DispatchTaskModel;
import com.zj.contract.json.ContractJson;
import com.zj.domain.entity.bo.log.DispatchLogBO;
import com.zj.domain.entity.bo.log.SubDispatchLogBO;
import com.zj.domain.entity.bo.pipeline.BindBranchBO;
import com.zj.domain.entity.bo.pipeline.PipelineActionBO;
import com.zj.domain.entity.bo.pipeline.PipelineBO;
import com.zj.domain.entity.bo.pipeline.PipelineHistoryBO;
import com.zj.domain.entity.bo.pipeline.PipelineNodeBO;
import com.zj.domain.repository.log.IDispatchLogCommandRepository;
import com.zj.domain.repository.log.ISubDispatchLogCommandRepository;
import com.zj.domain.repository.pipeline.IBindBranchQueryRepository;
import com.zj.domain.repository.pipeline.IPipelineActionQueryRepository;
import com.zj.domain.repository.pipeline.IPipelineHistoryCommandRepository;
import com.zj.domain.repository.pipeline.IPipelineNodeQueryRepository;
import com.zj.domain.repository.pipeline.IPipelineQueryRepository;
import com.zj.master.dispatch.IDispatchExecutor;
import com.zj.master.dispatch.pipeline.builder.RefreshContextBuilder;
import com.zj.master.dispatch.pipeline.builder.RequestContextBuilder;
import com.zj.contract.pipeline.node.NodeConfig;
import com.zj.contract.pipeline.node.RefreshContext;
import com.zj.contract.pipeline.node.RequestContext;
import com.zj.contract.pipeline.node.TaskNode;
import com.zj.master.entity.vo.ActionDetail;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
@Slf4j
@Component
public class PipelineDispatch implements IDispatchExecutor {

  private final IPipelineQueryRepository pipelineRepository;
  private final IPipelineNodeQueryRepository pipelineNodeRepository;
  private final IPipelineHistoryCommandRepository pipelineHistoryRepository;
  private final IPipelineActionQueryRepository pipelineActionRepository;
  private final UniqueIdService uniqueIdService;
  private final PipelineExecuteProxy pipelineExecuteProxy;
  private final ISubDispatchLogCommandRepository subDispatchLogRepository;
  private final IDispatchLogCommandRepository dispatchLogRepository;
  private final IBindBranchQueryRepository bindBranchRepository;

  public PipelineDispatch(IPipelineQueryRepository pipelineRepository,
                          IPipelineNodeQueryRepository pipelineNodeRepository,
                          IPipelineHistoryCommandRepository pipelineHistoryRepository,
                          IPipelineActionQueryRepository pipelineActionRepository, UniqueIdService uniqueIdService,
                          PipelineExecuteProxy pipelineExecuteProxy, ISubDispatchLogCommandRepository subDispatchLogRepository,
                          IDispatchLogCommandRepository dispatchLogRepository, IBindBranchQueryRepository bindBranchRepository) {
    this.pipelineRepository = pipelineRepository;
    this.pipelineNodeRepository = pipelineNodeRepository;
    this.pipelineHistoryRepository = pipelineHistoryRepository;
    this.pipelineActionRepository = pipelineActionRepository;
    this.uniqueIdService = uniqueIdService;
    this.pipelineExecuteProxy = pipelineExecuteProxy;
    this.subDispatchLogRepository = subDispatchLogRepository;
    this.dispatchLogRepository = dispatchLogRepository;
    this.bindBranchRepository = bindBranchRepository;
  }

  @Override
  public LogType type() {
    return LogType.PIPELINE;
  }

  @Override
  public boolean isExistInJvm(DispatchLogBO taskLog) {
    return pipelineExecuteProxy.isExitTask(taskLog.getSourceRecordId());
  }

  @Override
  public String dispatch(DispatchTaskModel task, String logId) {
    String pipelineId = task.firstSourceId();
    PipelineBO pipeline = pipelineRepository.getPipeline(pipelineId);
    if (Objects.isNull(pipeline)) {
      log.info("can not find pipeline name={} pipelineId={}", task.getSourceName(),
          pipelineId);
      return null;
    }

    List<PipelineNodeBO> pipelineNodes = pipelineNodeRepository.getPipelineNodes(
        pipeline.getPipelineId());
    if (CollectionUtils.isEmpty(pipelineNodes)) {
      log.info("can not find pipeline nodes name={} pipelineId={}", task.getSourceName(),
          pipelineId);
      return null;
    }

    String historyId = uniqueIdService.getUniqueId();
    saveHistory(pipeline.getPipelineId(), historyId);
    log.info("start run pipeline={} name={} historyId={}", pipelineId, task.getSourceName(),
        historyId);

    dispatchLogRepository.updateLogSourceRecord(logId, historyId);

    PipelineTask pipelineTask = new PipelineTask();
    pipelineTask.setPipelineId(pipeline.getPipelineId());
    pipelineTask.setHistoryId(historyId);
    pipelineTask.setLogId(logId);

    List<TaskNode> taskNodeList = pipelineNodes.stream()
        .sorted(Comparator.comparing(PipelineNodeBO::getSortOrder))
        .map(node -> buildTaskNode(node, historyId, pipeline.getServiceId()))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    if (CollectionUtils.isEmpty(taskNodeList)) {
      //所有节点的action都找不到，没有任何可执行内容，必须收尾而不是留一条永远RUNNING的历史
      log.warn("pipeline has no executable node, pipelineId={} historyId={}",
          pipeline.getPipelineId(), historyId);
      closeFinishedPipeline(logId, historyId, Collections.emptyList());
      return historyId;
    }

    pipelineTask.addAll(taskNodeList);
    //子任务日志只记真正参与执行的节点，action已丢失的节点不能建成永久RUNNING
    createSubTaskLog(taskNodeList, pipelineTask.getLogId());
    pipelineExecuteProxy.runTask(pipelineTask);
    return historyId;
  }

  private void createSubTaskLog(List<TaskNode> taskNodeList, String logId) {
    List<SubDispatchLogBO> logList = taskNodeList.stream().map(taskNode -> {
      SubDispatchLogBO subTaskLog = new SubDispatchLogBO();
      subTaskLog.setSubTaskId(uniqueIdService.getUniqueId());
      subTaskLog.setSubTaskName(taskNode.getName());
      subTaskLog.setLogId(logId);
      subTaskLog.setExecuteId(taskNode.getNodeId());
      subTaskLog.setExecuteType(taskNode.getExecuteType());
      subTaskLog.setStatus(ProcessStatus.RUNNING.getType());
      long dateNow = System.currentTimeMillis();
      subTaskLog.setCreateTime(dateNow);
      subTaskLog.setUpdateTime(dateNow);
      return subTaskLog;
    }).collect(Collectors.toList());
    subDispatchLogRepository.batchSaveLogs(logList);
  }

  private void saveHistory(String pipelineId, String historyId) {
    PipelineHistoryBO pipelineHistory = new PipelineHistoryBO();
    pipelineHistory.setHistoryId(historyId);
    pipelineHistory.setPipelineId(pipelineId);
    pipelineHistory.setPipelineStatus(ProcessStatus.RUNNING.getType());
    pipelineHistory.setPipelineConfig(null);
    pipelineHistory.setCreateTime(System.currentTimeMillis());
    pipelineHistory.setUpdateTime(System.currentTimeMillis());
    BindBranchBO bindBranch = bindBranchRepository.getPipelineBindBranch(pipelineId);
    //只有发布类流水线强制绑定分支，未绑定属正常状态，分支留空即可，不能让整条流水线跑不起来
    pipelineHistory.setBranch(Objects.isNull(bindBranch) ? null : bindBranch.getGitBranch());
    pipelineHistoryRepository.createPipelineHistory(pipelineHistory);
  }


  private TaskNode buildTaskNode(PipelineNodeBO pipelineNode, String historyId, String serviceId) {
    log.info("start build taskNode ={}", ContractJson.write(pipelineNode));
    TaskNode taskNode = new TaskNode();
    taskNode.setNodeId(pipelineNode.getNodeId());
    taskNode.setName(pipelineNode.getNodeName());
    taskNode.setExecuteTime(System.currentTimeMillis());
    taskNode.setHistoryId(historyId);
    taskNode.setServiceId(serviceId);

    ConfigDetail configDetail = pipelineNode.getConfigDetail();
    PipelineActionBO action = pipelineActionRepository.getAction(configDetail.getActionId());
    if (Objects.isNull(action)) {
      return null;
    }
    taskNode.setExecuteType(action.getExecuteType());

    ActionDetail actionDetail = new ActionDetail(configDetail, action);
    RequestContext requestContext = RequestContextBuilder.createContext(actionDetail);
    requestContext.setPipelineId(pipelineNode.getPipelineId());
    taskNode.setRequestContext(requestContext);

    RefreshContext refreshContext = RefreshContextBuilder.createContext(actionDetail);
    taskNode.setRefreshContext(refreshContext);

    NodeConfig nodeConfig = new NodeConfig();
    taskNode.setNodeConfig(nodeConfig);
    return taskNode;
  }

  @Override
  public boolean resume(DispatchLogBO dispatchLog) {
    if (CollectionUtils.isEmpty(dispatchLog.getSourceIds())) {
      log.info("resume task has no source id, logId={}", dispatchLog.getLogId());
      return false;
    }
    String pipelineId = dispatchLog.getSourceIds().get(0);
    PipelineBO pipeline = pipelineRepository.getPipeline(pipelineId);
    if (Objects.isNull(pipeline)) {
      log.info("resume task not find pipeline name={} pipelineId={}", dispatchLog.getSourceName(),
          pipelineId);
      return false;
    }

    List<PipelineNodeBO> pipelineNodes = pipelineNodeRepository.getPipelineNodes(
        pipeline.getPipelineId());
    if (CollectionUtils.isEmpty(pipelineNodes)) {
      log.info("can not find pipeline nodes name={} pipelineId={}", dispatchLog.getSourceName(),
          pipelineId);
      return false;
    }

    PipelineTask pipelineTask = new PipelineTask();
    pipelineTask.setPipelineId(pipeline.getPipelineId());
    pipelineTask.setLogId(dispatchLog.getLogId());
    if (StringUtils.isBlank(dispatchLog.getSourceRecordId())) {
      String newHistoryId = uniqueIdService.getUniqueId();
      saveHistory(pipelineId, newHistoryId);
      dispatchLog.setSourceRecordId(newHistoryId);
    }
    String historyId = dispatchLog.getSourceRecordId();
    pipelineTask.setHistoryId(historyId);

    //过滤掉已经执行完成的任务；找不到任何子任务日志说明还没跑过节点，需要全量重跑
    List<SubDispatchLogBO> subLogs = subDispatchLogRepository.getSubLogByLogId(
        dispatchLog.getLogId());
    List<String> unCompleteNodeIds = subLogs.stream()
        .filter(subTask -> !ProcessStatus.isCompleteStatus(subTask.getStatus()))
        .map(SubDispatchLogBO::getExecuteId).collect(Collectors.toList());
    List<PipelineNodeBO> runNodes = CollectionUtils.isEmpty(subLogs) ? pipelineNodes
        : pipelineNodes.stream().filter(node -> unCompleteNodeIds.contains(node.getNodeId()))
            .collect(Collectors.toList());
    //repository没有默认排序，重建时的节点顺序必须与首次调度一致
    List<TaskNode> taskNodeList = runNodes.stream()
        .sorted(Comparator.comparing(PipelineNodeBO::getSortOrder))
        .map(node -> buildTaskNode(node, historyId, pipeline.getServiceId()))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    if (CollectionUtils.isEmpty(taskNodeList)) {
      //没有任何可重跑的节点，不能交给执行器空跑：空任务同样会占住pipelineTaskMap，
      //之后isExitTask恒为true，这条日志连下次重建的机会都会失去
      log.info("resume pipeline has no node to run, logId={} historyId={} subLogSize={}",
          dispatchLog.getLogId(), historyId, subLogs.size());
      closeFinishedPipeline(dispatchLog.getLogId(), historyId, subLogs);
      return true;
    }

    pipelineTask.addAll(taskNodeList);
    if (CollectionUtils.isEmpty(subLogs)) {
      //如果找不到子任务执行记录，那么就需要重新创建，且只对真正执行的节点建
      createSubTaskLog(taskNodeList, dispatchLog.getLogId());
    }
    pipelineExecuteProxy.runTask(pipelineTask);
    return true;
  }

  /**
   * 没有节点可重跑时直接把历史与日志推到终态
   * 常见于节点已全部执行完、但状态收尾之前进程挂掉；这种状态下空跑会让流水线永久停在RUNNING
   * 结束监听器不重放，避免崩溃恢复时重复发通知
   * */
  private void closeFinishedPipeline(String logId, String historyId, List<SubDispatchLogBO> subLogs) {
    boolean allSuccess = CollectionUtils.isNotEmpty(subLogs) && subLogs.stream()
        .allMatch(subLog -> Objects.equals(subLog.getStatus(), ProcessStatus.SUCCESS.getType()));
    ProcessStatus finalStatus = allSuccess ? ProcessStatus.SUCCESS : ProcessStatus.FAIL;
    log.warn("close pipeline history without executing, historyId={} status={} subLogSize={}",
        historyId, finalStatus, subLogs.size());
    pipelineHistoryRepository.updateStatus(historyId, finalStatus);
    dispatchLogRepository.updateLogStatus(logId, finalStatus.getType());
  }

  @Override
  public Integer getExecuteCount() {
    return pipelineExecuteProxy.getTaskSize();
  }
}
