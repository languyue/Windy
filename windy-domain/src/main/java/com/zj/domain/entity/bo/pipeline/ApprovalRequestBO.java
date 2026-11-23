package com.zj.domain.entity.bo.pipeline;

import lombok.Data;

/**
 * @author guyuelan
 */
@Data
public class ApprovalRequestBO {

  private String requestId;

  private String recordId;

  private String nodeId;

  private String historyId;

  private String approver;

  /**
   * 审批结论，取 ProcessStatus.type
   * */
  private Integer approveStatus;

  private String comment;

  private Long createTime;

  private Long updateTime;
}
