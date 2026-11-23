package com.zj.domain.repository.pipeline;

import com.zj.contract.rpc.PageSize;
import com.zj.domain.entity.bo.pipeline.CodeChangeBO;
import java.util.List;

/**
* @author guyuelan
* @since 2023/5/18
*/
public interface ICodeChangeQueryRepository {

  /**
  * 获取代码变更
  * @param codeChangeId 代码变更ID
  * @return 代码变更信息
  */
  CodeChangeBO getCodeChange(String codeChangeId);

  /**
  * 获取服务代码变更
  *
  * @param serviceId 服务ID
  * @return 代码变更列表
  */
  List<CodeChangeBO> getServiceChanges(String serviceId);

  /**
  * 分页获取服务的变更列表
  * @param serviceId 服务ID
  * @param page 分页号
  * @param size 分页大小
  * @param name 变更名称
  * @return 变更分页列表
  */
  PageSize<CodeChangeBO> getServiceChangesPage(String serviceId, Integer page, Integer size , String name);

  /**
  * 获取关联代码变更
  * @param relationId 关联ID
  * @param relationType 关联类型
  * @return 代码变更列表
  */
  List<CodeChangeBO> getCodeChangeByRelationId(String relationId, Integer relationType);

}
