package com.zj.domain.repository.feature;

import com.zj.domain.entity.bo.feature.ExecutePointBO;
import java.util.List;

/**
* @author guyuelan
* @since 2023/5/17
*/
public interface IExecutePointQueryRepository {

  /**
  * 获取执行点
  * @param executePointId 执行点ID
  * @return 执行点
  */
  ExecutePointBO getExecutePoint(String executePointId);

  /**
  * 获取用例下的执行点
  * @param featureId 特性ID
  * @return 执行点列表
  */
  List<ExecutePointBO> getExecutePointByFeatureId(String featureId);

  /**
  * 根据用例ID列表获取执行点
  * @param featureIds 用例ID列表
  * @return 执行点列表
  */
  List<ExecutePointBO> getPointsByFeatureIds(List<String> featureIds);

  /**
  * 获取模板下的执行点
  * @param templateId 模板ID
  * @return 执行点列表
  */
  List<ExecutePointBO> getTemplateExecutePoints(String templateId);

  List<ExecutePointBO> getAllExecutePoint();

}
