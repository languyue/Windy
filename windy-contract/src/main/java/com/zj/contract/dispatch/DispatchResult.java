package com.zj.contract.dispatch;

import com.zj.contract.enums.DispatchStatus;
import lombok.Data;

/**
 * 调度任务的统一返回结构，各类型任务在此基础上补充自己的执行标识
 *
 * @author guyuelan
 * @since 2023/5/18
 */
@Data
public class DispatchResult {

  /**
   * 受理结果状态
   */
  private DispatchStatus status;

  /**
   * 结果描述，拒绝时需要说明原因
   */
  private String message;

  public DispatchResult() {
  }

  public DispatchResult(DispatchStatus status, String message) {
    this.status = status;
    this.message = message;
  }

  /**
   * 任务是否已被受理，命名不带 get 前缀，避免被当作 JSON 属性写进报文
   */
  public boolean accepted() {
    return DispatchStatus.ACCEPTED == status;
  }
}
