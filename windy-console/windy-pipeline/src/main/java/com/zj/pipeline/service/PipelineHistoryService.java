package com.zj.pipeline.service;

import com.zj.contract.port.IMasterCommand;
import com.zj.contract.rpc.command.PipelineHistoryCommand;
import com.zj.common.exception.ApiException;
import com.zj.contract.exception.ErrorCode;
import com.zj.domain.entity.bo.pipeline.NodeRecordBO;
import com.zj.domain.entity.bo.pipeline.NodeStatus;
import com.zj.domain.entity.bo.pipeline.PipelineBO;
import com.zj.domain.entity.bo.pipeline.PipelineExecuteInfo;
import com.zj.domain.entity.bo.pipeline.PipelineHistoryBO;
import com.zj.domain.repository.pipeline.IPipelineHistoryQueryRepository;
import com.zj.domain.repository.pipeline.IPipelineQueryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2021/10/15
 */
@Slf4j
@Service
public class PipelineHistoryService {
  private final NodeRecordService recordService;
  private final IPipelineQueryRepository pipelineRepository;
  private final IPipelineHistoryQueryRepository pipelineHistoryRepository;
  private final IMasterCommand masterCommand;

  public PipelineHistoryService(NodeRecordService recordService,
                                IPipelineQueryRepository pipelineRepository,
                                IPipelineHistoryQueryRepository pipelineHistoryRepository,
                                IMasterCommand masterCommand) {
    this.recordService = recordService;
    this.pipelineRepository = pipelineRepository;
    this.pipelineHistoryRepository = pipelineHistoryRepository;
    this.masterCommand = masterCommand;
  }

  public PipelineHistoryBO getPipelineHistory(String historyId) {
    return pipelineHistoryRepository.getPipelineHistory(historyId);
  }

  /**
   * pipeline_history 的写权在 master，console 只下发命令
   */
  public String createPipelineHistory(PipelineHistoryBO pipelineHistoryBO) {
    PipelineHistoryCommand command = new PipelineHistoryCommand();
    command.setPipelineId(pipelineHistoryBO.getPipelineId());
    command.setPipelineName(pipelineHistoryBO.getPipelineName());
    command.setBranch(pipelineHistoryBO.getBranch());
    command.setPipelineConfig(pipelineHistoryBO.getPipelineConfig());
    command.setPipelineStatus(pipelineHistoryBO.getPipelineStatus());
    String historyId = masterCommand.createPipelineHistory(command);
    return Objects.isNull(historyId) ? "" : historyId;
  }

  public List<PipelineHistoryBO> listPipelineHistories(String pipelineId) {
    checkPipeline(pipelineId);
    return pipelineHistoryRepository.listPipelineHistories(pipelineId);
  }

  public PipelineHistoryBO getLatestPipelineHistory(String serviceId, String pipelineId) {
    checkPipelineWithService(serviceId, pipelineId);
    return pipelineHistoryRepository.getLatestPipelineHistory(pipelineId);
  }

  private void checkPipelineWithService(String serviceId, String pipelineId) {
    PipelineBO pipelineBO = checkPipeline(pipelineId);
    if (!Objects.equals(pipelineBO.getServiceId(), serviceId)) {
      log.info("pipeline={} not belong serviceId={}", pipelineId, serviceId);
      throw new ApiException(ErrorCode.PIPELINE_NOT_BELONG_SERVICE);
    }
  }

  private PipelineBO checkPipeline(String pipelineId) {
    PipelineBO pipelineBO = pipelineRepository.getPipeline(pipelineId);
    if (Objects.isNull(pipelineBO)) {
      log.info("can not find pipeline={}", pipelineId);
      throw new ApiException(ErrorCode.NOT_FOUND_PIPELINE);
    }
    return pipelineBO;
  }

  public PipelineExecuteInfo getPipeLineStatusDetail(String historyId) {
    PipelineHistoryBO pipelineHistory = getPipelineHistory(historyId);
    List<NodeRecordBO> nodeRecords = recordService.getNodeRecordsByHistory(historyId);
    List<NodeStatus> statusList = nodeRecords.stream().map(nodeRecord -> {
      NodeStatus nodeStatus = new NodeStatus();
      nodeStatus.setRecordId(nodeRecord.getRecordId());
      nodeStatus.setNodeId(nodeRecord.getNodeId());
      nodeStatus.setStatus(nodeRecord.getStatus());
      nodeStatus.setMessage(nodeRecord.getResult());
      return nodeStatus;
    }).collect(Collectors.toList());

    return PipelineExecuteInfo.builder().pipelineStatus(pipelineHistory.getPipelineStatus())
        .nodeStatusList(statusList).build();
  }

  public boolean deleteByPipelineId(String pipelineId) {
    checkPipeline(pipelineId);
    return masterCommand.deletePipelineHistory(pipelineId);
  }
}
