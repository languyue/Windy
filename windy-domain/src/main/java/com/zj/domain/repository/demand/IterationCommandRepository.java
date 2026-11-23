package com.zj.domain.repository.demand;

import com.zj.domain.entity.bo.demand.IterationBO;
import java.util.List;
import com.zj.domain.repository.marker.ConfigCommandRepository;


/**
 * @author guyuelan
 */
public interface IterationCommandRepository extends IterationQueryRepository, ConfigCommandRepository {

  /**
  * 创建迭代
  * @param iterationBO 迭代信息
  * @return 是否成功
  */
  IterationBO createIteration(IterationBO iterationBO);

  /**
  * 删除迭代
  * @param iterationId 迭代ID
  * @return 是否成功
  */
  boolean deleteIteration(String iterationId);

  /**
  * 更新迭代
  * @param iterationBO 迭代信息
  * @return 是否成功
  */
  boolean updateIteration(IterationBO iterationBO);

}
