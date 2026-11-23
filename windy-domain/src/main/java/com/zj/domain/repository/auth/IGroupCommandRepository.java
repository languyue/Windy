package com.zj.domain.repository.auth;

import com.zj.domain.entity.bo.auth.GroupBO;
import java.util.List;
import com.zj.domain.repository.marker.ConfigCommandRepository;


/**
 * @author guyuelan
 */
public interface IGroupCommandRepository extends IGroupQueryRepository, ConfigCommandRepository {

  /**
  * 创建组织
  */
  boolean createGroup(GroupBO groupBO);

  /**
  * 更新组织
  */
  boolean updateGroup(GroupBO groupBO);

  /**
  * 删除组织
  */
  boolean deleteGroup(String groupId);

}
