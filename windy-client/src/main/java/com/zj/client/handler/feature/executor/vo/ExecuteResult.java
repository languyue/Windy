package com.zj.client.handler.feature.executor.vo;

import com.zj.client.entity.bo.ExecuteRecord;
import com.zj.client.entity.bo.FeatureHistory;
import lombok.Data;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/12
 */
@Data
public class ExecuteResult {

  private FeatureHistory featureHistory;

  private List<ExecuteRecord> executeRecords;

  public ExecuteResult(FeatureHistory featureHistory, List<ExecuteRecord> executeRecords) {
    this.featureHistory = featureHistory;
    this.executeRecords = executeRecords;
  }
}
