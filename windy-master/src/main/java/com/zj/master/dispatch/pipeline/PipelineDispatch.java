package com.zj.master.dispatch.pipeline;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.generate.UniqueIdService;
import com.zj.domain.entity.dto.pipeline.PipelineActionDto;
import com.zj.domain.entity.dto.pipeline.PipelineDTO;
import com.zj.domain.entity.dto.pipeline.PipelineHistoryDto;
import com.zj.domain.entity.dto.pipeline.PipelineNodeDTO;
import com.zj.domain.repository.pipeline.IPipelineActionRepository;
import com.zj.domain.repository.pipeline.IPipelineHistoryRepository;
import com.zj.domain.repository.pipeline.IPipelineNodeRepository;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import com.zj.master.dispatch.IDispatchExecutor;
import com.zj.master.dispatch.pipeline.builder.RefreshContextBuilder;
import com.zj.master.dispatch.pipeline.builder.RequestContextBuilder;
import com.zj.master.entity.dto.TaskDetailDto;
import com.zj.master.entity.enums.LogType;
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
 * @author falcon
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
  private ExecuteProxy executeProxy;

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

    List<PipelineNodeDTO> pipelineNodes = pipelineNodeRepository.getPipelineNodes(
        pipeline.getPipelineId());
    if (CollectionUtils.isEmpty(pipelineNodes)) {
      log.info("can not find pipeline nodes name={} pipelineId={}", task.getSourceName(),
          task.getSourceId());
      return false;
    }

    log.info("start run pipeline={} name={}", task.getSourceId(), task.getSourceName());
    String historyId = uniqueIdService.getUniqueId();
    saveHistory(pipeline.getPipelineId(), historyId);

    PipelineTask pipelineTask = new PipelineTask();
    pipelineTask.setPipelineId(pipeline.getPipelineId());
    pipelineTask.setHistoryId(historyId);

    List<TaskNode> taskNodeList = pipelineNodes.stream().map(this::buildTaskNode)
        .collect(Collectors.toList());
    pipelineTask.addAll(taskNodeList);
    executeProxy.runTask(pipelineTask);
    return true;
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


  private TaskNode buildTaskNode(PipelineNodeDTO pipelineNode) {
    log.info("start build taskNode ={}", JSON.toJSONString(pipelineNode));
    TaskNode taskNode = new TaskNode();
    taskNode.setNodeId(pipelineNode.getNodeId());
    taskNode.setName(pipelineNode.getNodeName());
    taskNode.setExecuteTime(System.currentTimeMillis());

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
}
