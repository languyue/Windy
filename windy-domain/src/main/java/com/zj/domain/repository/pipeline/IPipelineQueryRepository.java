package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.bo.pipeline.PipelineBO;
import java.util.List;

/**
* @author guyuelan
* @since 2023/5/15
*/
public interface IPipelineQueryRepository {

  /**
  * 获取流水线
  * @param pipelineId 流水线ID
  * @return 流水线信息
  */
  PipelineBO getPipeline(String pipelineId);

  /**
  * 获取服务流水线
  * @param serviceId 服务ID
  * @return 流水线列表
  */
  List<PipelineBO> getServicePipelines(String serviceId);

  /**
  * 获取发布类型流水线
  * @param serviceId 服务ID
  * @return 流水线信息
  */
  PipelineBO getPublishPipeline(String serviceId);

  /**
  * 获取定时流水线
  * @return 流水线列表
  */
  List<PipelineBO> getSchedulePipelines();

}
