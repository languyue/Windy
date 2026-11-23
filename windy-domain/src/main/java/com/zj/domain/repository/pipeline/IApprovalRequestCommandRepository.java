package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.bo.pipeline.ApprovalRequestBO;
import com.zj.domain.repository.marker.RuntimeCommandRepository;

/**
 * 审批工单写接口，唯一写 owner 是 windy-master。
 *
 * console 只提交审批命令，由 master 落工单并推进 node_record 状态。
 *
 * @author guyuelan
 */
public interface IApprovalRequestCommandRepository extends IApprovalRequestQueryRepository,
    RuntimeCommandRepository {

  /**
   * 提交审批结论，同一节点记录重复提交时覆盖上一次结论
   *
   * @param approvalRequest 审批工单
   * @return 是否成功
   */
  boolean submitApproval(ApprovalRequestBO approvalRequest);

  /**
   * 删除一次流水线运行产生的审批工单
   *
   * @param historyId 流水线历史ID
   * @return 是否成功
   */
  boolean deleteByHistoryId(String historyId);
}
