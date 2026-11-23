package com.zj.domain.repository.pipeline;

import com.zj.contract.rpc.PageSize;
import com.zj.domain.entity.bo.pipeline.CodeChangeBO;
import java.util.List;
import com.zj.domain.repository.marker.ConfigCommandRepository;

/**
* @author guyuelan
* @since 2023/5/18
*/
public interface ICodeChangeCommandRepository extends ICodeChangeQueryRepository, ConfigCommandRepository {

  /**
  * 保存代码变更
  * @param codeChange 代码变更信息
  * @return 是否成功
  */
  boolean saveCodeChange(CodeChangeBO codeChange);

  /**
  * 更新代码变更
  * @param codeChange 代码变更信息
  * @return 是否成功
  */
  boolean updateCodeChange(CodeChangeBO codeChange);

  /**
  * 删除代码变更
  * @param codeChangeId 代码变更ID
  * @return 是否成功
  */
  boolean deleteCodeChange(String codeChangeId);

  /**
  * 批量删除代码变更
  * @param codeChangeIds 代码变更ID列表
  * @return 是否成功
  */
  boolean batchDeleteCodeChange(List<String> codeChangeIds);

}
