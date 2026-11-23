package com.zj.domain.entity.po.pipeline;

import lombok.Data;

/**
 * 审批工单。
 *
 * 审批结论过去直接改写 node_record.status，导致同一个节点状态机被 console 与 master 同时推进。
 * 拆出本表后 node_record.status 只由 master 推进，本表记录"谁在什么时候批了什么"。
 *
 * @author guyuelan
 */
@Data
public class ApprovalRequest {

  private Long id;

  /**
   * 审批工单Id
   * */
  private String requestId;

  /**
   * 被审批的节点执行记录Id
   * */
  private String recordId;

  /**
   * 流水线节点Id
   * */
  private String nodeId;

  /**
   * 所属流水线记录Id
   * */
  private String historyId;

  /**
   * 审批人
   * */
  private String approver;

  /**
   * 审批结论，取 ProcessStatus.type
   * */
  private Integer approveStatus;

  /**
   * 审批意见
   * */
  private String comment;

  private Long createTime;

  private Long updateTime;
}
