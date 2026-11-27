package com.zj.contract.dispatch;

import com.zj.contract.enums.DispatchStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用例、测试集任务的调度结果
 *
 * @author guyuelan
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FeatureDispatchResult extends DispatchResult {

  /**
   * 本次执行的记录Id：用例任务为任务执行记录Id，批量执行用例为触发记录Id或临时Id
   */
  private String recordId;

  public FeatureDispatchResult() {
  }

  private FeatureDispatchResult(DispatchStatus status, String message, String recordId) {
    super(status, message);
    this.recordId = recordId;
  }

  public static FeatureDispatchResult accepted(String recordId) {
    return new FeatureDispatchResult(DispatchStatus.ACCEPTED, "任务已受理", recordId);
  }

  public static FeatureDispatchResult rejected(String message) {
    return new FeatureDispatchResult(DispatchStatus.REJECTED, message, null);
  }
}
