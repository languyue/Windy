package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.bo.pipeline.PipelineNodeBO;
import java.util.List;

/**
* @author guyuelan
* @since 2023/5/15
*/
public interface IPipelineNodeQueryRepository {

  /**
  * 获取流水线节点
  * @param pipelineNodeId 节点ID
  * @return 节点信息
  */
  PipelineNodeBO getPipelineNode(String pipelineNodeId);

  /**
  * 获取流水线节点
  * @param pipelineId 流水线ID
  * @return 节点列表
  */
  List<PipelineNodeBO> getPipelineNodes(String pipelineId);

}
