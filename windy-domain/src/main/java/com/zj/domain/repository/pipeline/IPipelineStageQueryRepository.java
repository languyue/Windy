package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.bo.pipeline.PipelineStageBO;
import java.util.List;

/**
* @author guyuelan
* @since 2023/5/19
*/
public interface IPipelineStageQueryRepository {

  /**
  * 获取流水线阶段
  * @param stageId 阶段ID
  * @return 阶段信息
  */
  PipelineStageBO getPipelineStage(String stageId);

  /**
  * 排序流水线节点
  * @param pipelineId 流水线ID
  * @return 节点列表
  */
  List<PipelineStageBO> sortPipelineNodes(String pipelineId);

}
