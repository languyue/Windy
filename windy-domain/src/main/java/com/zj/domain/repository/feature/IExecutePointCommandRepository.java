package com.zj.domain.repository.feature;

import com.zj.domain.entity.bo.feature.ExecutePointBO;
import java.util.List;
import com.zj.domain.repository.marker.ConfigCommandRepository;

/**
* @author guyuelan
* @since 2023/5/17
*/
public interface IExecutePointCommandRepository extends IExecutePointQueryRepository, ConfigCommandRepository {

  /**
  * 更新执行点
  * @param executePoint 执行点
  * @return 是否成功
  */
  boolean updateExecutePoint(ExecutePointBO executePoint);

  /**
  * 删除执行点
  * @param pointId 执行点ID
  * @return 是否成功
  */
  boolean deleteExecutePoint(String pointId);

  /**
  * 删除用例下的执行点
  * @param featureId 特性ID
  * @return 是否成功
  */
  boolean deleteByFeatureId(String featureId);

  /**
  * 根据用例ID列表删除执行点
  * @param featureIds 用例ID列表
  * @return 是否成功
  */
  boolean deleteByFeatureIds(List<String> featureIds);

  /**
  * 保存执行点
  * @param executePoint 执行点
  * @return 是否成功
  */
  boolean saveExecutePoint(ExecutePointBO executePoint);

  /**
  * 批量保存执行点
  * @param executePointBOList 执行点列表
  * @return 是否成功
  */
  boolean batchSavePoints(List<ExecutePointBO> executePointBOList);

  /**
  * 批量更新执行点
  * @param executePointBOList 执行点列表
  * @return 是否成功
  */
  boolean updateBatch(List<ExecutePointBO> executePointBOList);

}
