package com.zj.contract.dispatch;

import com.zj.contract.enums.DispatchStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 流水线任务的调度结果
 *
 * @author guyuelan
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PipelineDispatchResult extends DispatchResult {

  /**
   * 本次执行的流水线历史Id
   */
  private String historyId;

  public PipelineDispatchResult() {
  }

  private PipelineDispatchResult(DispatchStatus status, String message, String historyId) {
    super(status, message);
    this.historyId = historyId;
  }

  public static PipelineDispatchResult accepted(String historyId) {
    return new PipelineDispatchResult(DispatchStatus.ACCEPTED, "任务已受理", historyId);
  }

  public static PipelineDispatchResult rejected(String message) {
    return new PipelineDispatchResult(DispatchStatus.REJECTED, message, null);
  }
}
