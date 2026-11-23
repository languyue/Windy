package com.zj.domain.repository.auth;

import com.zj.domain.entity.bo.auth.GroupBO;
import java.util.List;


/**
 * @author guyuelan
 */
public interface IGroupQueryRepository {

  /**
  * 获取组织列表
  */
  List<GroupBO> getGroups();

  /**
  * 根据ID获取组织
  */
  GroupBO getGroup(String groupId);

}
