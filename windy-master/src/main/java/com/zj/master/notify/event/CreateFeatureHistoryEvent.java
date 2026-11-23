package com.zj.master.notify.event;

import com.zj.contract.enums.NotifyType;
import com.zj.contract.event.ResultEvent;
import com.zj.domain.entity.bo.feature.FeatureHistoryBO;
import com.zj.domain.entity.bo.feature.FeatureInfoBO;
import com.zj.domain.repository.feature.IFeatureHistoryCommandRepository;
import com.zj.domain.repository.feature.IFeatureQueryRepository;
import com.zj.domain.repository.log.ISubDispatchLogCommandRepository;
import com.zj.master.notify.INotifyEvent;
import com.zj.master.notify.NotifyPayloadMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Slf4j
@Component
public class CreateFeatureHistoryEvent implements INotifyEvent {

  private final IFeatureHistoryCommandRepository featureHistoryRepository;
  private final IFeatureQueryRepository featureRepository;
  private final ISubDispatchLogCommandRepository subDispatchLogRepository;

  public CreateFeatureHistoryEvent(IFeatureHistoryCommandRepository featureHistoryRepository,
                                   IFeatureQueryRepository featureRepository, ISubDispatchLogCommandRepository subDispatchLogRepository) {
    this.featureHistoryRepository = featureHistoryRepository;
    this.featureRepository = featureRepository;
    this.subDispatchLogRepository = subDispatchLogRepository;
  }

  @Override
  public NotifyType type() {
    return NotifyType.CREATE_FEATURE_HISTORY;
  }

  @Override
  public boolean handle(ResultEvent resultEvent) {
    log.info("receive feature history create event id = {} event={}", resultEvent.getExecuteId(),
        resultEvent.getParams());
    FeatureHistoryBO featureHistoryBO = NotifyPayloadMapper.toFeatureHistoryBO(resultEvent);
    FeatureInfoBO feature = featureRepository.getFeatureById(featureHistoryBO.getFeatureId());
    Optional.ofNullable(feature).ifPresent(f -> featureHistoryBO.setFeatureName(f.getFeatureName()));

    subDispatchLogRepository.updateSubLogClientIp(resultEvent.getLogId(),
        featureHistoryBO.getFeatureId(), resultEvent.getClientIp());

    featureHistoryBO.setExecuteStatus(resultEvent.getStatus().getType());
    return featureHistoryRepository.saveHistory(featureHistoryBO);
  }
}
