package com.zj.feature.service;

import com.zj.contract.port.IMasterCommand;
import com.zj.domain.entity.bo.feature.FeatureHistoryBO;
import com.zj.domain.repository.feature.IFeatureHistoryQueryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * feature_history 属于运行态域，console 只读；删除一律下发命令给 master。
 */
@Slf4j
@Service
public class FeatureHistoryService {
  private final IFeatureHistoryQueryRepository featureHistoryRepository;
  private final IMasterCommand masterCommand;

  public FeatureHistoryService(IFeatureHistoryQueryRepository featureHistoryRepository,
      IMasterCommand masterCommand) {
    this.featureHistoryRepository = featureHistoryRepository;
    this.masterCommand = masterCommand;
  }

  public List<FeatureHistoryBO> featureHistories(String featureId) {
    return featureHistoryRepository.featureHistories(featureId);
  }

  public boolean deleteHistory(String historyId) {
    return masterCommand.deleteFeatureHistory(historyId);
  }

  public List<FeatureHistoryBO> getHistories(String taskId) {
    return featureHistoryRepository.getHistoriesByTaskRecordId(taskId);
  }
}
