package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.bo.pipeline.PipelineBO;
import java.util.List;
import com.zj.domain.repository.marker.ConfigCommandRepository;

/**
* @author guyuelan
* @since 2023/5/15
*/
public interface IPipelineCommandRepository extends IPipelineQueryRepository, ConfigCommandRepository {

  /**
  * 更新流水线
  * @param pipelineBO 流水线信息
  * @return 是否成功
  */
  boolean updatePipeline(PipelineBO pipelineBO);

  /**
  * 创建流水线
  * @param pipelineBO 流水线信息
  * @return 是否成功
  */
  boolean createPipeline(PipelineBO pipelineBO);

  /**
  * 删除流水线
  * @param pipelineId 流水线ID
  * @return 是否成功
  */
  boolean deletePipeline(String pipelineId);

}
