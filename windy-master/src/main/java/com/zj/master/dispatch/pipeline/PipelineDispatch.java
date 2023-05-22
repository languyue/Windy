package com.zj.master.dispatch.pipeline;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.generate.UniqueIdService;
import com.zj.domain.entity.dto.log.SubTaskLogDto;
import com.zj.domain.entity.dto.log.TaskLogDto;
import com.zj.domain.entity.dto.pipeline.PipelineActionDto;
import com.zj.domain.entity.dto.pipeline.PipelineDTO;
import com.zj.domain.entity.dto.pipeline.PipelineHistoryDto;
import com.zj.domain.entity.dto.pipeline.PipelineNodeDto;
import com.zj.domain.entity.po.log.SubTaskLog;
import com.zj.domain.repository.log.ISubTaskLogRepository;
import com.zj.domain.repository.log.ITaskLogRepository;
import com.zj.domain.repository.pipeline.IPipelineActionRepository;
import com.zj.domain.repository.pipeline.IPipelineHistoryRepository;
import com.zj.domain.repository.pipeline.IPipelineNodeRepository;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import com.zj.master.dispatch.IDispatchExecutor;
import com.zj.master.dispatch.pipeline.builder.RefreshContextBuilder;
import com.zj.master.dispatch.pipeline.builder.RequestContextBuilder;
import com.zj.master.entity.dto.TaskDetailDto;
import com.zj.common.enums.LogType;
import com.zj.master.entity.vo.ActionDetail;
import com.zj.master.entity.vo.ConfigDetail;
import com.zj.master.entity.vo.NodeConfig;
import com.zj.master.entity.vo.RefreshContext;
import com.zj.master.entity.vo.RequestContext;
import com.zj.master.entity.vo.TaskNode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
@Slf4j
@Component
public class PipelineDispatch implements IDispatchExecutor {

  @Autowired
  private IPipelineRepository pipelineRepository;

  @Autowired
  private IPipelineNodeRepository pipelineNodeRepository;

  @Autowired
  private IPipelineHistoryRepository pipelineHistoryRepository;

  @Autowired
  private IPipelineActionRepository pipelineActionRepository;

  @Autowired
  private UniqueIdService uniqueIdService;

  @Autowired
  private PipelineExecuteProxy pipelineExecuteProxy;

  @Autowired
  private ISubTaskLogRepository subTaskLogRepository;

  @Override
  public Integer type() {
    return LogType.PIPELINE.getType();
  }

  @Override
  public boolean dispatch(TaskDetailDto task) {
    PipelineDTO pipeline = pipelineRepository.getPipeline(task.getSourceId());
    if (Objects.isNull(pipeline)) {
      log.info("can not find pipeline name={} pipelineId={}", task.getSourceName(),
          task.getSourceId());
      return false;
    }

    List<PipelineNodeDto> pipelineNodes = pipelineNodeRepository.getPipelineNodes(
        pipeline.getPipelineId());
    if (CollectionUtils.isEmpty(pipelineNodes)) {
      log.info("can not find pipeline nodes name={} pipelineId={}", task.getSourceName(),
          task.getSourceId());
      return false;
    }

    String historyId = uniqueIdService.getUniqueId();
    saveHistory(pipeline.getPipelineId(), historyId);
    log.info("start run pipeline={} name={} historyId={}", task.getSourceId(), task.getSourceName(),
        historyId);

    PipelineTask pipelineTask = new PipelineTask();
    pipelineTask.setPipelineId(pipeline.getPipelineId());
    pipelineTask.setHistoryId(historyId);
    pipelineTask.setLogId(task.getTaskLogId());

    List<TaskNode> taskNodeList = pipelineNodes.stream().map(node -> buildTaskNode(node, historyId))
        .collect(Collectors.toList());
    pipelineTask.addAll(taskNodeList);

    createSubTaskLog(pipelineTask);
    pipelineExecuteProxy.runTask(pipelineTask);
    return true;
  }

  private void createSubTaskLog(PipelineTask pipelineTask) {
    List<SubTaskLogDto> logList = pipelineTask.getTaskNodes().stream().map(taskNode -> {
      SubTaskLogDto subTaskLog = new SubTaskLogDto();
      subTaskLog.setSubTaskId(uniqueIdService.getUniqueId());
      subTaskLog.setLogId(pipelineTask.getLogId());
      subTaskLog.setExecuteId(taskNode.getNodeId());
      subTaskLog.setStatus(ProcessStatus.RUNNING.getType());
      long dateNow = System.currentTimeMillis();
      subTaskLog.setCreateTime(dateNow);
      subTaskLog.setUpdateTime(dateNow);
      return subTaskLog;
    }).collect(Collectors.toList());

    subTaskLogRepository.batchSaveLogs(logList);
  }

  private void saveHistory(String pipelineId, String historyId) {
    PipelineHistoryDto pipelineHistory = new PipelineHistoryDto();
    pipelineHistory.setHistoryId(historyId);
    pipelineHistory.setPipelineId(pipelineId);
    pipelineHistory.setPipelineStatus(ProcessStatus.RUNNING.getType());
    pipelineHistory.setPipelineConfig("");
    pipelineHistory.setBranch("master");
    pipelineHistory.setExecutor("admin");
    pipelineHistory.setCreateTime(System.currentTimeMillis());
    pipelineHistory.setUpdateTime(System.currentTimeMillis());
    pipelineHistoryRepository.createPipelineHistory(pipelineHistory);
  }


  private TaskNode buildTaskNode(PipelineNodeDto pipelineNode, String historyId) {
    log.info("start build taskNode ={}", JSON.toJSONString(pipelineNode));
    TaskNode taskNode = new TaskNode();
    taskNode.setNodeId(pipelineNode.getNodeId());
    taskNode.setName(pipelineNode.getNodeName());
    taskNode.setExecuteTime(System.currentTimeMillis());
    taskNode.setHistoryId(historyId);

    ConfigDetail configDetail = JSON.parseObject(pipelineNode.getConfigDetail(),
        ConfigDetail.class);
    PipelineActionDto action = pipelineActionRepository.getAction(configDetail.getActionId());
    taskNode.setExecuteType(action.getExecuteType());

    ActionDetail actionDetail = new ActionDetail(configDetail, action);
    RequestContext requestContext = RequestContextBuilder.createContext(actionDetail);
    taskNode.setRequestContext(requestContext);

    RefreshContext refreshContext = RefreshContextBuilder.createContext(actionDetail);
    taskNode.setRefreshContext(refreshContext);

    NodeConfig nodeConfig = new NodeConfig();
    taskNode.setNodeConfig(nodeConfig);
    return taskNode;
  }

  @Override
  public boolean resume(TaskLogDto taskLog) {
    PipelineDTO pipeline = pipelineRepository.getPipeline(taskLog.getSourceId());
    if (Objects.isNull(pipeline)) {
      log.info("can not find pipeline name={} pipelineId={}", taskLog.getSourceName(),
          taskLog.getSourceId());
      return false;
    }

    List<PipelineNodeDto> pipelineNodes = pipelineNodeRepository.getPipelineNodes(
        pipeline.getPipelineId());
    if (CollectionUtils.isEmpty(pipelineNodes)) {
      log.info("can not find pipeline nodes name={} pipelineId={}", taskLog.getSourceName(),
          taskLog.getSourceId());
      return false;
    }

    PipelineTask pipelineTask = new PipelineTask();
    pipelineTask.setPipelineId(pipeline.getPipelineId());
    pipelineTask.setHistoryId(taskLog.getSourceRecordId());
    pipelineTask.setLogId(taskLog.getLogId());

    List<String> subTasks = subTaskLogRepository.getSubTaskByLogId(taskLog.getLogId()).stream()
        .filter(subTask -> !ProcessStatus.isCompleteStatus(subTask.getStatus()))
        .map(SubTaskLogDto::getExecuteId).collect(
            Collectors.toList());

    List<TaskNode> taskNodeList = pipelineNodes.stream()
        .filter(node -> !subTasks.contains(node.getNodeId()))
        .map(node -> buildTaskNode(node, taskLog.getSourceRecordId()))
        .collect(Collectors.toList());
    pipelineTask.addAll(taskNodeList);
    pipelineExecuteProxy.runTask(pipelineTask);
    return true;
  }
}
