package com.zj.domain.repository.auth;

import com.zj.contract.rpc.PageSize;
import com.zj.domain.entity.bo.auth.UserBO;
import java.util.List;


/**
 * @author guyuelan
 */
public interface IUserQueryRepository {

  /**
  * 根据用户名称查询用户
  * @param userName 用户名称
  * @return 用户信息
  */
  UserBO getUserByUserName(String userName);

  /**
  * 根据用户ID查询用户
  * @param userId 用户ID
  * @return 用户信息
  */
  UserBO getUserByUserId(String userId);

  /**
  * 分页获取组织用户列表
  * @param page 页码
  * @param size 每页数量
  * @return 用户列表
  */
  PageSize<UserBO> getGroupUserPage(String groupId, Integer page, Integer size);

  /**
  * 获取组织所有用户列表
  * @param groupId 组织ID
  * @return 用户列表
  */
  List<UserBO> getGroupUserList(String groupId);

  /**
  * 获取用户列表
  * @param userIds 用户ID列表
  * @return 用户信息
  */
  List<UserBO> getUserByUserList(List<String> userIds);

  /**
  * 模糊查询用户名称
  * @param name 用户名称
  * @return 用户列表
  */
  List<UserBO> fuzzyQueryUserName(String name);

}
