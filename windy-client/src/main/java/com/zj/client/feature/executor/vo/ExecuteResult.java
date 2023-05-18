package com.zj.client.feature.executor.vo;

import com.zj.client.entity.po.ExecuteRecord;
import com.zj.client.entity.po.FeatureHistory;
import java.util.List;
import lombok.Data;

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
