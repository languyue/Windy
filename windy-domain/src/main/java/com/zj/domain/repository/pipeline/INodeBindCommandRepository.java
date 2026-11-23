package com.zj.domain.repository.pipeline;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.domain.entity.bo.pipeline.NodeBindBO;
import java.util.List;
import com.zj.domain.repository.marker.ConfigCommandRepository;

/**
* @author guyuelan
* @since 2023/5/19
*/
public interface INodeBindCommandRepository extends INodeBindQueryRepository, ConfigCommandRepository {

  /**
  * 保存流水线节点绑定
  * @param nodeBindBO 节点信息
  * @return 是否成功
  */
  boolean saveNodeBind(NodeBindBO nodeBindBO);

  /**
  * 更新流水线节点绑定
  * @param nodeBindBO 节点信息
  * @return 是否成功
  */
  boolean updateNode(NodeBindBO nodeBindBO);

  /**
  * 删除流水线节点
  * @param nodeId 节点ID
  * @return 是否成功
  */
  Boolean deleteNode(String nodeId);

}
