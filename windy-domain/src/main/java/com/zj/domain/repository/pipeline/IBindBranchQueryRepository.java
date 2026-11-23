package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.bo.pipeline.BindBranchBO;
import java.util.List;

/**
* @author guyuelan
* @since 2023/5/18
*/
public interface IBindBranchQueryRepository {

  /**
  * 获取流水线绑定的分支
  * @param pipelineId 流水线ID
  * @return 分支列表
  */
  List<BindBranchBO> getPipelineRelatedBranches(String pipelineId);

  /**
  * 获取流水线已绑定分支
  * @param pipelineId 流水线ID
  * @return 分支信息
  */
  BindBranchBO getPipelineBindBranch(String pipelineId);

  /**
  * 获取绑定分支
  * @param bindId 绑定ID
  * @return 分支信息
  */
  BindBranchBO getGitBranch(String bindId);

}
