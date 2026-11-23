package com.zj.domain.repository.auth;

import com.zj.contract.rpc.PageSize;
import com.zj.domain.entity.bo.auth.ResourceBO;
import java.util.List;
import com.zj.domain.repository.marker.ConfigCommandRepository;


/**
 * @author guyuelan
 */
public interface IResourceCommandRepository extends IResourceQueryRepository, ConfigCommandRepository {

  /**
  * 创建资源
  * @param resourceBO 资源信息
  * @return 是否成功
  */
  boolean createResource(ResourceBO resourceBO);

  /**
  * 更新资源
  * @param resourceBO 资源信息
  * @return 是否成功
  */
  boolean updateResource(ResourceBO resourceBO);

  /**
  * 根据ID删除资源
  * @param resourceId 资源ID
  * @return 是否成功
  */
  boolean deleteResource(String resourceId);

  /**
  * 绑定资源
  * @param relationId 关联ID
  * @param resourceIds 资源ID列表
  * @return 是否成功
  */
  boolean resourceBind(String relationId, List<String> resourceIds);

  /**
  * 解绑资源
  * @param roleId 角色ID
  * @return 是否成功
  */
  boolean unbindResource(String roleId);

}
