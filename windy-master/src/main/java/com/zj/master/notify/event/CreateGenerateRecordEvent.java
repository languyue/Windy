package com.zj.master.notify.event;

import com.zj.contract.event.ResultEvent;
import com.zj.contract.generate.GenerateDetail;
import com.zj.common.entity.generate.GenerateRecordBO;
import com.zj.contract.enums.NotifyType;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.repository.service.IGenerateRecordCommandRepository;
import com.zj.master.notify.INotifyEvent;
import com.zj.master.notify.NotifyPayloadMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class CreateGenerateRecordEvent implements INotifyEvent {

  private final IGenerateRecordCommandRepository generateRecordRepository;

  public CreateGenerateRecordEvent(IGenerateRecordCommandRepository generateRecordRepository) {
    this.generateRecordRepository = generateRecordRepository;
  }

  @Override
  public NotifyType type() {
    return NotifyType.CREATE_GENERATE_MAVEN;
  }

  @Override
  public boolean handle(ResultEvent resultEvent) {
    log.info("receive generate create record ={}", resultEvent.getParams());
    GenerateRecordBO recordDto = NotifyPayloadMapper.toGenerateRecordBO(resultEvent);
    if (Objects.isNull(recordDto)) {
      return false;
    }
    List<GenerateRecordBO> generateRecords = generateRecordRepository.getGenerateRecord(recordDto.getServiceId(),
            recordDto.getVersion());
    if (CollectionUtils.isEmpty(generateRecords)){
      GenerateDetail generateDetail = recordDto.getGenerateParams();
      recordDto.setVersion(generateDetail.getVersion());
      return generateRecordRepository.create(recordDto);
    }
    return true;
  }
}
