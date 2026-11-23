package com.zj.master.service;

import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.common.entity.command.ApproveCommand;
import com.zj.common.entity.command.PipelineHistoryCommand;
import com.zj.common.entity.command.TaskRecordCommand;
import com.zj.domain.entity.bo.pipeline.ApprovalRequestBO;
import com.zj.domain.entity.bo.pipeline.PipelineHistoryBO;
import com.zj.domain.entity.bo.feature.TaskRecordBO;
import com.zj.domain.entity.bo.auth.UserBO;
import com.zj.domain.repository.feature.IFeatureHistoryCommandRepository;
import com.zj.domain.repository.feature.ITaskRecordCommandRepository;
import com.zj.domain.repository.auth.IUserQueryRepository;
import com.zj.domain.repository.pipeline.IApprovalRequestCommandRepository;
import com.zj.domain.repository.pipeline.INodeRecordCommandRepository;
import com.zj.domain.repository.pipeline.IPipelineHistoryCommandRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 运行态域的命令门面。
 *
 * console 需要改变运行态表时全部经过这里，一张表只有一个进程能写的不变量因此在 master 侧收口。
 *
 * @author guyuelan
 */
@Slf4j
@Service
public class MasterCommandService {

  private final INodeRecordCommandRepository nodeRecordRepository;
  private final IApprovalRequestCommandRepository approvalRequestRepository;
  private final IPipelineHistoryCommandRepository pipelineHistoryRepository;
  private final ITaskRecordCommandRepository taskRecordRepository;
  private final IFeatureHistoryCommandRepository featureHistoryRepository;
  private final IUserQueryRepository userRepository;
  private final UniqueIdService uniqueIdService;

  public MasterCommandService(INodeRecordCommandRepository nodeRecordRepository,
      IApprovalRequestCommandRepository approvalRequestRepository,
      IPipelineHistoryCommandRepository pipelineHistoryRepository,
      ITaskRecordCommandRepository taskRecordRepository,
      IFeatureHistoryCommandRepository featureHistoryRepository,
      IUserQueryRepository userRepository,
      UniqueIdService uniqueIdService) {
    this.nodeRecordRepository = nodeRecordRepository;
    this.approvalRequestRepository = approvalRequestRepository;
    this.pipelineHistoryRepository = pipelineHistoryRepository;
    this.taskRecordRepository = taskRecordRepository;
    this.featureHistoryRepository = featureHistoryRepository;
    this.userRepository = userRepository;
    this.uniqueIdService = uniqueIdService;
  }

  /**
   * 审批结论先落成可审计的工单，再由 master 推进节点状态，node_record 不会被两个进程同时改写。
   */
  @Transactional
  public boolean approveNode(ApproveCommand command) {
    if (Objects.isNull(command) || Objects.isNull(command.getRecordId())) {
      log.info("approve command has no recordId, ignore");
      return false;
    }

    ApprovalRequestBO approvalRequest = new ApprovalRequestBO();
    approvalRequest.setRequestId(uniqueIdService.getUniqueId());
    approvalRequest.setRecordId(command.getRecordId());
    approvalRequest.setNodeId(command.getNodeId());
    approvalRequest.setHistoryId(command.getHistoryId());
    approvalRequest.setApprover(command.getApprover());
    approvalRequest.setApproveStatus(command.getStatus());
    approvalRequest.setComment(command.getComment());
    boolean submitWork = approvalRequestRepository.submitApproval(approvalRequest);

    List<String> messageList = Collections.singletonList(Objects.toString(command.getComment(), ""));
    boolean updateStatus = nodeRecordRepository.updateNodeRecordStatus(command.getRecordId(),
        command.getStatus(), messageList);
    log.info("approve node recordId={} workOrder={} status={}", command.getRecordId(), submitWork, updateStatus);
    return updateStatus;
  }

  public String createPipelineHistory(PipelineHistoryCommand command) {
    if (Objects.isNull(command) || Objects.isNull(command.getPipelineId())) {
      return "";
    }

    String historyId = uniqueIdService.getUniqueId();
    PipelineHistoryBO pipelineHistory = new PipelineHistoryBO();
    pipelineHistory.setHistoryId(historyId);
    pipelineHistory.setPipelineId(command.getPipelineId());
    pipelineHistory.setPipelineName(command.getPipelineName());
    pipelineHistory.setBranch(command.getBranch());
    pipelineHistory.setPipelineConfig(command.getPipelineConfig());
    pipelineHistory.setPipelineStatus(command.getPipelineStatus());
    return pipelineHistoryRepository.createPipelineHistory(pipelineHistory) ? historyId : "";
  }

  /**
   * 临时批量执行的 task_record 由 master 创建，master 侧推进状态时依赖这一行存在
   */
  public String createTaskRecord(TaskRecordCommand command) {
    if (Objects.isNull(command) || Objects.isNull(command.getTestCaseId())) {
      return "";
    }

    String recordId = uniqueIdService.getUniqueId();
    TaskRecordBO taskRecord = new TaskRecordBO();
    taskRecord.setRecordId(recordId);
    taskRecord.setTriggerId(command.getTriggerId());
    taskRecord.setType(command.getType());
    taskRecord.setExecuteConfig(command.getTaskConfig());
    taskRecord.setTestCaseId(command.getTestCaseId());
    taskRecord.setTaskName(command.getTaskName());
    taskRecord.setStatus(command.getStatus());
    taskRecord.setUserId(command.getUserId());
    taskRecord.setPercent(0);
    taskRecord.setCreateTime(System.currentTimeMillis());
    taskRecord.setUpdateTime(System.currentTimeMillis());
    Optional.ofNullable(command.getUserId()).ifPresent(userId -> {
      UserBO user = userRepository.getUserByUserId(userId);
      if (Objects.nonNull(user)) {
        taskRecord.setExecuteUser(Optional.ofNullable(user.getNickName()).orElseGet(user::getUserName));
      }
    });
    return taskRecordRepository.save(taskRecord) ? recordId : "";
  }

  @Transactional
  public boolean deletePipelineHistory(String pipelineId) {
    boolean deleteHistory = pipelineHistoryRepository.deleteByPipelineId(pipelineId);
    //工单挂在 history 上，先按 pipeline 取出所有 history 再逐条清理
    List<String> historyIds = pipelineHistoryRepository.listPipelineHistories(pipelineId).stream()
        .map(PipelineHistoryBO::getHistoryId).collect(Collectors.toList());
    historyIds.forEach(historyId -> approvalRequestRepository.deleteByHistoryId(historyId));
    log.info("delete pipeline history pipelineId={} history={} approvalCount={}", pipelineId, deleteHistory,
        historyIds.size());
    return deleteHistory;
  }

  public boolean deleteNodeRecords(List<String> nodeIds) {
    if (CollectionUtils.isEmpty(nodeIds)) {
      return true;
    }
    return nodeRecordRepository.deleteRecordByNodeId(nodeIds);
  }

  @Transactional
  public boolean deleteTaskRecord(String recordId) {
    boolean deleteFeatureHistory = featureHistoryRepository.deleteByRecordId(recordId);
    log.info("delete feature history by task record={} result={}", recordId, deleteFeatureHistory);
    return taskRecordRepository.deleteTaskRecord(recordId);
  }

  public boolean deleteFeatureHistory(String historyId) {
    return featureHistoryRepository.deleteHistory(historyId);
  }
}
