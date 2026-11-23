package com.zj.contract.event.payload;

import lombok.Data;

/**
 * 用例执行历史记录的通知载荷。
 *
 * @author guyuelan
 */
@Data
public class FeatureHistoryPayload implements NotifyPayload {

  private String historyId;

  /**
   * 用例Id
   * */
  private String featureId;

  /**
   * 执行记录Id
   * */
  private String recordId;

  /**
   * 用例名称
   * */
  private String featureName;

  /**
   * 用例执行人
   * */
  private String executor;

  /**
   * 执行状态
   * */
  private Integer executeStatus;

  /**
   * 创建时间
   * */
  private Long createTime;
}
