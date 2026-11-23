package com.zj.domain.repository.pipeline;

import com.zj.contract.enums.ProcessStatus;
import com.zj.domain.entity.bo.pipeline.NodeRecordBO;
import java.util.List;

/**
* @author guyuelan
* @since 2023/5/15
*/
public interface INodeRecordQueryRepository {

  /**
  * 获取节点执行记录
  * @param pipelineHistoryId 流水线历史ID
  * @return 节点记录列表
  */
  List<NodeRecordBO> getRecordsByHistoryId(String pipelineHistoryId);

  /**
  * 获取节点执行记录
  * @param nodeRecordId 记录ID
  * @return 节点记录
  */
  NodeRecordBO getRecordById(String nodeRecordId);

  /**
  * 获取流水线节点执行记录
  * @param pipelineHistoryId 流水线历史ID
  * @param nodeId 节点ID
  * @return 节点记录
  */
  NodeRecordBO getRecordsByNodeIdAndHistory(String pipelineHistoryId, String nodeId);

}
