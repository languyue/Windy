package com.zj.contract.rpc;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/3/29
 */
@Data
public class ResponseStatusModel {

  private Integer status;

  /**
   * 任务执行进度，用例任务按执行成功的用例数占比统计
   * */
  private PercentStatics data;

  private String message;

  @Data
  public static class PercentStatics{

    private Integer percent;
  }
}
