package com.zj.domain.repository.demand;

import com.zj.domain.entity.bo.demand.SpaceBO;
import java.util.List;


/**
 * @author guyuelan
 */
public interface ISpaceQueryRepository {

  /**
  * 获取空间列表
  * @return 空间列表
  */
  List<SpaceBO> getSpaceList();

  /**
  * 获取空间详情
  * @param spaceId 空间ID
  * @return 空间信息
  */
  SpaceBO getSpace(String spaceId);

}
