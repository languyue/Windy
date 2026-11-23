package com.zj.master.notify.event;

import com.zj.common.enums.ExecuteType;
import com.zj.contract.enums.NotifyType;
import com.zj.contract.enums.ProcessStatus;
import com.zj.contract.event.ResultEvent;
import com.zj.domain.entity.bo.pipeline.NodeRecordBO;
import com.zj.domain.repository.log.ISubDispatchLogCommandRepository;
import com.zj.domain.repository.pipeline.INodeRecordCommandRepository;
import com.zj.master.dispatch.pipeline.PipelineExecuteProxy;
import com.zj.master.notify.INotifyEvent;
import com.zj.master.notify.NotifyPayloadMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Slf4j
@Service
public class UpdateNodeRecordEvent implements INotifyEvent {

  private final INodeRecordCommandRepository nodeRecordRepository;
  private final PipelineExecuteProxy pipelineExecuteProxy;
  private final ISubDispatchLogCommandRepository subTaskLogRepository;

  public UpdateNodeRecordEvent(INodeRecordCommandRepository nodeRecordRepository,
      PipelineExecuteProxy pipelineExecuteProxy, ISubDispatchLogCommandRepository subTaskLogRepository) {
    this.nodeRecordRepository = nodeRecordRepository;
    this.pipelineExecuteProxy = pipelineExecuteProxy;
    this.subTaskLogRepository = subTaskLogRepository;
  }

  @Override
  public NotifyType type() {
    return NotifyType.UPDATE_NODE_RECORD;
  }

  @Override
  public boolean handle(ResultEvent resultEvent) {
    log.info("receive node record update event id = {} event={} status={}", resultEvent.getExecuteId(),
        resultEvent.getExecuteType(), resultEvent.getStatus());
    NodeRecordBO oldNodeRecord = nodeRecordRepository.getRecordById(resultEvent.getExecuteId());
    if (ProcessStatus.isCompleteStatus(oldNodeRecord.getStatus()) && !Objects.equals(
        ExecuteType.APPROVAL.name(), resultEvent.getExecuteType())) {
      //如果任务已经是完结状态，不能再更新记录
      //审批节点完成需要由负责人在console界面操作而不是在通知中完成，所以审批完成可以继续执行下一个节点
      log.info("node record status completed, not update recordId={}", oldNodeRecord.getRecordId());
      return true;
    }

    NodeRecordBO nodeRecord = NotifyPayloadMapper.toNodeRecordBO(resultEvent);
    nodeRecord.setRecordId(resultEvent.getExecuteId());
    nodeRecord.setStatus(resultEvent.getStatus().getType());
    Optional.ofNullable(resultEvent.getContext()).ifPresent(
        context -> nodeRecord.setPipelineContext(resultEvent.getContext()));

    boolean updateStatus = nodeRecordRepository.updateNodeRecord(nodeRecord);
    subTaskLogRepository.updateLogStatus(resultEvent.getLogId(), nodeRecord.getNodeId(),
        nodeRecord.getStatus());

    //单个节点状态变化要通知给执行者，然后执行一下节点的任务
    pipelineExecuteProxy.statusChange(nodeRecord);
    return updateStatus;
  }
}
