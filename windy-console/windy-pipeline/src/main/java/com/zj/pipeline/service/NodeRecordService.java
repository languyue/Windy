package com.zj.pipeline.service;

import com.zj.common.adapter.auth.IAuthService;
import com.zj.contract.port.IMasterCommand;
import com.zj.contract.rpc.command.ApproveCommand;
import com.zj.contract.enums.ProcessStatus;
import com.zj.common.exception.ApiException;
import com.zj.contract.exception.ErrorCode;
import com.zj.domain.entity.bo.pipeline.NodeRecordBO;
import com.zj.domain.repository.pipeline.INodeRecordQueryRepository;
import java.util.List;
import java.util.Objects;

import com.zj.pipeline.entity.dto.ApprovalInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2022/5/24
 */
@Slf4j
@Component
public class NodeRecordService {

  private final INodeRecordQueryRepository nodeRecordRepository;
  private final IMasterCommand masterCommand;
  private final IAuthService authService;

  public NodeRecordService(INodeRecordQueryRepository nodeRecordRepository, IMasterCommand masterCommand,
      IAuthService authService) {
    this.nodeRecordRepository = nodeRecordRepository;
    this.masterCommand = masterCommand;
    this.authService = authService;
  }

  /**
   * 审批结论下发给 master，由它落审批工单并推进 node_record，console 不再直接写运行态表
   */
  public Boolean approval(ApprovalInfo approvalInfo) {
    ProcessStatus processStatus = ProcessStatus.exchange(approvalInfo.getType());
    if (Objects.isNull(processStatus)) {
      log.warn("can not parse status type ={}", approvalInfo.getType());
      throw new ApiException(ErrorCode.PARAM_VALIDATE_ERROR);
    }
    NodeRecordBO nodeRecord = nodeRecordRepository.getRecordsByNodeIdAndHistory(approvalInfo.getHistoryId(),
            approvalInfo.getNodeId());
    if (Objects.isNull(nodeRecord)) {
      log.info("can not find node record historyId={} nodeId={}",approvalInfo.getHistoryId(), approvalInfo.getNodeId());
      return false;
    }

    ApproveCommand command = new ApproveCommand();
    command.setRecordId(nodeRecord.getRecordId());
    command.setHistoryId(approvalInfo.getHistoryId());
    command.setNodeId(approvalInfo.getNodeId());
    command.setStatus(processStatus.getType());
    command.setComment(approvalInfo.getMessage());
    command.setApprover(authService.getCurrentUserId());
    return masterCommand.approveNode(command);
  }

  public List<NodeRecordBO> getNodeRecordsByHistory(String historyId) {
    return nodeRecordRepository.getRecordsByHistoryId(historyId);
  }
}
