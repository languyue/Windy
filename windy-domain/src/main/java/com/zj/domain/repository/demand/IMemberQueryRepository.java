package com.zj.domain.repository.demand;

import com.zj.domain.entity.bo.auth.UserBO;
import com.zj.domain.entity.bo.service.ResourceMemberBO;
import com.zj.domain.entity.enums.MemberType;
import java.util.List;


/**
 * @author guyuelan
 */
public interface IMemberQueryRepository {

  /**
  * 根据关联ID和成员类型获取成员列表
  * @param relationId 关联ID
  * @param memberType 成员类型
  * @return  成员列表
  */
  List<ResourceMemberBO> getByRelationMember(String relationId, String memberType);

  /**
  * 根据资源ID和成员类型获取成员列表
  * @param resourceId 资源ID
  * @param memberType 成员类型
  * @return 成员列表
  */
  List<ResourceMemberBO> getResourceRelations(String resourceId, String memberType);

  /**
  * 根据资源ID和成员类型获取用户列表
  * @param resourceId 资源ID
  * @param memberType 成员类型
  * @return 成员列表
  */
  List<UserBO> getResourceUserMembers(String resourceId, String memberType);

}
