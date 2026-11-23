package com.zj.master.notify.event;

import com.zj.contract.enums.NotifyType;
import com.zj.contract.event.ResultEvent;
import com.zj.common.entity.generate.GenerateRecordBO;
import com.zj.domain.repository.service.IGenerateRecordCommandRepository;
import java.util.Objects;

import com.zj.master.notify.INotifyEvent;
import com.zj.master.notify.NotifyPayloadMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UpdateGenerateRecordEvent implements INotifyEvent {

  private final IGenerateRecordCommandRepository generateRecordRepository;

  public UpdateGenerateRecordEvent(IGenerateRecordCommandRepository generateRecordRepository) {
    this.generateRecordRepository = generateRecordRepository;
  }

  @Override
  public NotifyType type() {
    return NotifyType.UPDATE_GENERATE_MAVEN;
  }

  @Override
  public boolean handle(ResultEvent resultEvent) {
    log.info("receive generate record update event={}", resultEvent.getParams());
    GenerateRecordBO recordDto = NotifyPayloadMapper.toGenerateRecordBO(resultEvent);
    if (Objects.isNull(recordDto)) {
      return false;
    }

    GenerateRecordBO update = new GenerateRecordBO();
    update.setRecordId(resultEvent.getExecuteId());
    update.setGenerateResult(recordDto.getGenerateResult());
    update.setStatus(resultEvent.getStatus().getType());
    return generateRecordRepository.update(update);
  }
}
