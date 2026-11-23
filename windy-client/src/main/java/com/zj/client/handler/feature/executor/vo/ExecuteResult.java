package com.zj.client.handler.feature.executor.vo;

import com.zj.contract.event.payload.ExecuteRecordPayload;
import com.zj.contract.event.payload.FeatureHistoryPayload;
import lombok.Data;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/12
 */
@Data
public class ExecuteResult {

  private FeatureHistoryPayload featureHistory;

  private List<ExecuteRecordPayload> executeRecords;

  public ExecuteResult(FeatureHistoryPayload featureHistory, List<ExecuteRecordPayload> executeRecords) {
    this.featureHistory = featureHistory;
    this.executeRecords = executeRecords;
  }
}
