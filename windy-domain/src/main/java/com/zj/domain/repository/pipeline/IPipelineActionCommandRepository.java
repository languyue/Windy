package com.zj.domain.repository.pipeline;

import com.zj.contract.rpc.PageSize;
import com.zj.domain.entity.bo.pipeline.PipelineActionBO;
import java.util.List;
import com.zj.domain.repository.marker.ConfigCommandRepository;

/**
* @author guyuelan
* @since 2023/5/15
*/
public interface IPipelineActionCommandRepository extends IPipelineActionQueryRepository, ConfigCommandRepository {

  /**
  * 创建流水线动作
  * @param actionDto 动作信息
  * @return 是否成功
  */
  Boolean createAction(PipelineActionBO actionDto);

  /**
  * 更新流水线动作
  * @param actionDto 动作信息
  * @return 是否成功
  */
  Boolean updateAction(PipelineActionBO actionDto);

  /**
  * 绑定动作节点
  * @param nodeId 节点ID
  * @param actionIds 动作ID列表
  * @return 是否成功
  */
  Boolean actionsBindNode(String nodeId, List<String> actionIds);

  /**
  * 删除流水线动作
  * @param actionId 动作ID
  * @return 是否成功
  */
  Boolean deleteAction(String actionId);

  /**
  * 批量删除动作
  * @param actionIds 动作ID列表
  * @return 是否成功
  */
  boolean batchDelete(List<String> actionIds);

}
