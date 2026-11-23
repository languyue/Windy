package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.bo.pipeline.ApprovalRequestBO;

import java.util.List;

/**
 * 审批工单只读视图，console 与 master 都可以注入。
 *
 * @author guyuelan
 */
public interface IApprovalRequestQueryRepository {

  /**
   * 获取节点记录对应的审批工单
   *
   * @param recordId 节点执行记录ID
   * @return 审批工单，没有则返回 null
   */
  ApprovalRequestBO getByRecordId(String recordId);

  /**
   * 获取一次流水线运行中的所有审批工单
   *
   * @param historyId 流水线历史ID
   * @return 审批工单列表
   */
  List<ApprovalRequestBO> listByHistoryId(String historyId);
}
