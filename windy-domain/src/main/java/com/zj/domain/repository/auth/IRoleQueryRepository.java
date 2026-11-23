package com.zj.domain.repository.auth;

import com.zj.contract.rpc.PageSize;
import com.zj.domain.entity.bo.auth.RoleBO;
import java.util.List;


/**
 * @author guyuelan
 */
public interface IRoleQueryRepository {

  /**
  * 分页获取角色列表
  * @param page 页码
  * @param size 每页数量
  * @return 角色列表
  */
  PageSize<RoleBO> getRolePage(Integer page, Integer size);

  /**
  * 根据ID获取角色
  * @param roleId 角色ID
  * @return 角色信息
  */
  RoleBO getRole(String roleId);

  /**
  * 分页获取组织角色列表
  * @param groupId 组织ID
  * @return 角色列表
  */
  PageSize<RoleBO> getGroupRolePage(String groupId, Integer page, Integer size);

  /**
  * 获取所有角色
  * @return 角色列表
  */
  List<RoleBO> getAllRoles();

  /**
  * 判断角色是否已绑定
  * @param roleId 角色ID
  * @return 是否已绑定
  */
  boolean isRoleBind(String roleId);

}
