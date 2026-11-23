package com.zj.domain.repository.demand;

import com.zj.domain.entity.bo.demand.IterationBO;
import java.util.List;


/**
 * @author guyuelan
 */
public interface IterationQueryRepository {

  /**
  * 获取空间迭代列表
  * @param spaceId 空间ID
  * @return 迭代列表
  */
  List<IterationBO> getIterationList(String spaceId, List<String> iterationIds);

  /**
  * 获取空间未处理迭代
  * @param spaceId 空间ID
  * @return 迭代列表
  */
  List<IterationBO> getSpaceNotHandleIterations(String spaceId);

  /**
  * 获取迭代详情
  * @param iterationId 迭代ID
  * @return 迭代信息
  */
  IterationBO getIteration(String iterationId);

}
