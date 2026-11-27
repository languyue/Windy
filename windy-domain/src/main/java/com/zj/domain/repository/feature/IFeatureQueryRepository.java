package com.zj.domain.repository.feature;

import com.zj.contract.rpc.PageSize;
import com.zj.domain.entity.bo.feature.FeatureInfoBO;
import java.util.List;

/**
* @author guyuelan
* @since 2023/5/17
*/
public interface IFeatureQueryRepository {

  /**
  * 根据测试集获取用例列表
  * @param testCaseId 测试集ID
  * @return 用例列表
  */
  List<FeatureInfoBO> queryFeatureList(String testCaseId);

  /**
  * 根据测试集获取不包含文件夹的用例列表
  * @param testCaseId 测试集ID
  * @return 用例列表
  */
  List<FeatureInfoBO> queryNotContainFolder(String testCaseId);

  /**
  * 根据用例ID获取用例信息
  * @param featureId 用例ID
  * @return 用例信息
  */
  FeatureInfoBO getFeatureById(String featureId);

  /**
  * 批量获取用例信息
  * @param featureIds 用例ID列表
  * @return 用例信息列表
  */
  List<FeatureInfoBO> queryFeatureList(List<String> featureIds);

  /**
  * 分页查询测试集下用例信息
  * @param testCaseId 测试集ID
  * @param page 页码
  * @param size 每页数量
  * @return 用例信息列表
  */
  PageSize<FeatureInfoBO> queryFeaturePage(String testCaseId, int page, int size);

  /**
  * 获取用例的子用例列表
  * @param featureId 父用例ID
  * @return 子用例列表
  */
  List<FeatureInfoBO> getSubFeatures(String featureId);

  /**
  * 批量获取测试集下用例信息
  * @param testCaseIds  测试集ID列表
  * @return 用例信息列表
  */
  List<FeatureInfoBO> getFeatureByCases(List<String> testCaseIds);

}
