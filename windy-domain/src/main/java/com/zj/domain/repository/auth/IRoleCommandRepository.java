package com.zj.domain.repository.auth;

import com.zj.contract.rpc.PageSize;
import com.zj.domain.entity.bo.auth.RoleBO;
import java.util.List;
import com.zj.domain.repository.marker.ConfigCommandRepository;


/**
 * @author guyuelan
 */
public interface IRoleCommandRepository extends IRoleQueryRepository, ConfigCommandRepository {

  /**
  * 创建角色
  * @param roleBO 角色信息
  * @return 是否成功
  */
  Boolean createRole(RoleBO roleBO);

  /**
  * 更新角色
  * @param roleBO 角色信息
  * @return 是否成功
  */
  Boolean updateRole(RoleBO roleBO);

  /**
  * 删除角色
  * @param roleId 角色ID
  * @return 是否成功
  */
  Boolean deleteRole(String roleId);

  /**
  * 绑定角色
  * @param relationId 关联ID
  * @param roleIds 角色ID列表
  * @return 是否成功
  */
  boolean bindRole(String relationId, List<String> roleIds);

  /**
  * 解绑角色
  * @param relationId 关联ID
  * @return 是否成功
  */
  boolean unbind(String relationId);

}
