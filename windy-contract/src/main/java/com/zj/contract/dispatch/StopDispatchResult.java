package com.zj.contract.dispatch;

import com.zj.contract.enums.DispatchStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 停止任务的结果，停止是异步下发的，受理只代表停止指令已经发出
 *
 * @author guyuelan
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StopDispatchResult extends DispatchResult {

  public StopDispatchResult() {
  }

  private StopDispatchResult(DispatchStatus status, String message) {
    super(status, message);
  }

  public static StopDispatchResult accepted(String message) {
    return new StopDispatchResult(DispatchStatus.ACCEPTED, message);
  }

  public static StopDispatchResult rejected(String message) {
    return new StopDispatchResult(DispatchStatus.REJECTED, message);
  }
}
