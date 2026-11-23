package com.zj.master.notify.event;

import com.zj.contract.enums.NotifyType;
import com.zj.contract.event.ResultEvent;
import com.zj.domain.entity.bo.pipeline.NodeRecordBO;
import com.zj.domain.repository.log.ISubDispatchLogCommandRepository;
import com.zj.domain.repository.pipeline.INodeRecordCommandRepository;
import com.zj.master.notify.INotifyEvent;
import com.zj.master.notify.NotifyPayloadMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Slf4j
@Service
public class CreateNodeRecordEvent implements INotifyEvent {

  private final INodeRecordCommandRepository nodeRecordRepository;
  private final ISubDispatchLogCommandRepository subDispatchLogRepository;

  public CreateNodeRecordEvent(INodeRecordCommandRepository nodeRecordRepository,
      ISubDispatchLogCommandRepository subDispatchLogRepository) {
    this.nodeRecordRepository = nodeRecordRepository;
    this.subDispatchLogRepository = subDispatchLogRepository;
  }

  @Override
  public NotifyType type() {
    return NotifyType.CREATE_NODE_RECORD;
  }

  @Override
  public boolean handle(ResultEvent resultEvent) {
    log.info("receive node record create event id = {} event={}", resultEvent.getExecuteId(),
        resultEvent.getParams());
    NodeRecordBO nodeRecord = NotifyPayloadMapper.toNodeRecordBO(resultEvent);

    subDispatchLogRepository.updateSubLogClientIp(resultEvent.getLogId(), nodeRecord.getNodeId(),
        resultEvent.getClientIp());

    return nodeRecordRepository.saveNodeRecord(nodeRecord);
  }
}
