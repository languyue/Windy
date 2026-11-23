package com.zj.domain.repository.auth;

import com.zj.contract.rpc.PageSize;
import com.zj.domain.entity.bo.auth.UserBO;
import java.util.List;
import com.zj.domain.repository.marker.ConfigCommandRepository;


/**
 * @author guyuelan
 */
public interface IUserCommandRepository extends IUserQueryRepository, ConfigCommandRepository {

  /**
  * 创建用户
  * @param userBO 用户信息
  * @return 是否成功
  */
  Boolean createUser(UserBO userBO);

  /**
  * 更新用户
  * @param userBO 用户信息
  * @return 是否成功
  */
  Boolean updateUser(UserBO userBO);

  /**
  * 删除用户
  * @param userId 用户ID
  * @return 是否成功
  */
  Boolean deleteUser(String userId);

}
