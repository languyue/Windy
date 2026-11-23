package com.zj.master.notify.event;

import com.zj.contract.enums.NotifyType;
import com.zj.contract.enums.ProcessStatus;
import com.zj.contract.event.ResultEvent;
import com.zj.domain.entity.bo.feature.FeatureHistoryBO;
import com.zj.domain.repository.feature.IFeatureHistoryCommandRepository;
import com.zj.domain.repository.log.ISubDispatchLogCommandRepository;
import com.zj.master.dispatch.task.FeatureExecuteProxy;
import com.zj.master.notify.INotifyEvent;
import com.zj.master.notify.NotifyPayloadMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Slf4j
@Component
public class UpdateFeatureHistoryEvent implements INotifyEvent {

  private final IFeatureHistoryCommandRepository featureHistoryRepository;
  private final FeatureExecuteProxy featureExecuteProxy;
  private final ISubDispatchLogCommandRepository subTaskLogRepository;

  public UpdateFeatureHistoryEvent(IFeatureHistoryCommandRepository featureHistoryRepository,
      FeatureExecuteProxy featureExecuteProxy, ISubDispatchLogCommandRepository subTaskLogRepository) {
    this.featureHistoryRepository = featureHistoryRepository;
    this.featureExecuteProxy = featureExecuteProxy;
    this.subTaskLogRepository = subTaskLogRepository;
  }

  @Override
  public NotifyType type() {
    return NotifyType.UPDATE_FEATURE_HISTORY;
  }

  @Override
  public boolean handle(ResultEvent resultEvent) {
    log.info("receive update feature history event id = {} event={}", resultEvent.getExecuteId(),
        resultEvent);
    FeatureHistoryBO history = NotifyPayloadMapper.toFeatureHistoryBO(resultEvent);
    FeatureHistoryBO featureHistory = featureHistoryRepository.getFeatureHistory(history.getHistoryId());
    boolean updateStatus = true;
    if (!ProcessStatus.isCompleteStatus(featureHistory.getExecuteStatus())) {
      log.info("feature history status completed, update historyId={}", history.getHistoryId());
      updateStatus = featureHistoryRepository.updateStatus(history.getHistoryId(), resultEvent.getStatus().getType());
    }

    subTaskLogRepository.updateLogStatus(resultEvent.getLogId(), history.getHistoryId(),
            history.getExecuteStatus());
    featureExecuteProxy.featureStatusChange(resultEvent.getExecuteId(), history,  resultEvent.getContext());
    return updateStatus;
  }
}
