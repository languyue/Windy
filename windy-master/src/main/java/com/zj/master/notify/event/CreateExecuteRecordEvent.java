package com.zj.master.notify.event;

import com.zj.contract.enums.NotifyType;
import com.zj.contract.event.ResultEvent;
import com.zj.domain.entity.bo.feature.ExecuteRecordBO;
import com.zj.domain.repository.feature.IExecuteRecordCommandRepository;
import com.zj.master.notify.INotifyEvent;
import com.zj.master.notify.NotifyPayloadMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Slf4j
@Component
public class CreateExecuteRecordEvent implements INotifyEvent {

  private final IExecuteRecordCommandRepository executeRecordRepository;

  public CreateExecuteRecordEvent(IExecuteRecordCommandRepository executeRecordRepository) {
    this.executeRecordRepository = executeRecordRepository;
  }

  @Override
  public NotifyType type() {
    return NotifyType.CREATE_EXECUTE_POINT_RECORD;
  }

  @Override
  public boolean handle(ResultEvent resultEvent) {
    log.info("receive execute record create event id = {} event={}", resultEvent.getExecuteId(),
        resultEvent.getParams());
    ExecuteRecordBO executeRecord = NotifyPayloadMapper.toExecuteRecordBO(resultEvent);
    return executeRecordRepository.saveRecord(executeRecord);
  }
}
