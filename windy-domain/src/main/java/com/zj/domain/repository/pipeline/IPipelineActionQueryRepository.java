package com.zj.domain.repository.pipeline;

import com.zj.contract.rpc.PageSize;
import com.zj.domain.entity.bo.pipeline.PipelineActionBO;
import java.util.List;

/**
* @author guyuelan
* @since 2023/5/15
*/
public interface IPipelineActionQueryRepository {

  /**
  * 获取流水线动作
  * @param actionId 动作ID
  * @return 动作信息
  */
  PipelineActionBO getAction(String actionId);

  /**
  * 分页获取流水线动作
  * @param page 页码
  * @param size 每页数量
  * @param name 动作名称
  * @return 动作列表
  */
  PageSize<PipelineActionBO> getActions(Integer page, Integer size, String name);

  /**
  * 获取节点动作
  * @param nodeId 节点ID
  * @return 动作列表
  */
  List<PipelineActionBO> getActionsByNodeId(String nodeId);

}
