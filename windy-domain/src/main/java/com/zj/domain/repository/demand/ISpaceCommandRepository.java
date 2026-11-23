package com.zj.domain.repository.demand;

import com.zj.domain.entity.bo.demand.SpaceBO;
import java.util.List;
import com.zj.domain.repository.marker.ConfigCommandRepository;


/**
 * @author guyuelan
 */
public interface ISpaceCommandRepository extends ISpaceQueryRepository, ConfigCommandRepository {

  /**
  * 创建空间
  * @param spaceBO 空间信息
  * @return 是否成功
  */
  SpaceBO createSpace(SpaceBO spaceBO);

  /**
  * 更新空间
  * @param spaceBO 空间信息
  * @return 是否成功
  */
  boolean updateSpace(SpaceBO spaceBO);

  /**
  * 删除空间
  * @param spaceId 空间ID
  * @return 是否成功
  */
  boolean deleteSpace(String spaceId);

}
