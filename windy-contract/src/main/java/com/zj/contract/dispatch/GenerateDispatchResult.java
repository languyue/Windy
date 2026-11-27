package com.zj.contract.dispatch;

import com.zj.contract.enums.DispatchStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 生成二方包任务的调度结果
 *
 * @author guyuelan
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GenerateDispatchResult extends DispatchResult {

  public GenerateDispatchResult() {
  }

  private GenerateDispatchResult(DispatchStatus status, String message) {
    super(status, message);
  }

  public static GenerateDispatchResult accepted(String message) {
    return new GenerateDispatchResult(DispatchStatus.ACCEPTED, message);
  }

  public static GenerateDispatchResult rejected(String message) {
    return new GenerateDispatchResult(DispatchStatus.REJECTED, message);
  }
}
