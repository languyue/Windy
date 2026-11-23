package com.zj.domain.repository.feature;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.domain.entity.bo.feature.FeatureInfoBO;
import java.util.List;
import com.zj.domain.repository.marker.ConfigCommandRepository;

/**
* @author guyuelan
* @since 2023/5/17
*/
public interface IFeatureCommandRepository extends IFeatureQueryRepository, ConfigCommandRepository {

  /**
  * 保存用例信息
  * @param featureInfoBO 用例信息
  * @return 是否成功
  */
  boolean createFeature(FeatureInfoBO featureInfoBO);

  /**
  * 更新用例信息
  * @param featureInfoBO 用例信息
  * @return 是否成功
  */
  boolean updateFeatureInfo(FeatureInfoBO featureInfoBO);

  /**
  * 删除用例信息
  * @param featureId 用例ID
  * @return 是否成功
  */
  boolean deleteByFeatureId(String featureId);

  /**
  * 批量删除用例信息
  * @param featureIds 用例ID列表
  * @return 是否成功
  */
  boolean batchDeleteByFeatureId(List<String> featureIds);

  /**
  * 批量保存用例信息
  * @param featureInfoList 用例信息列表
  * @return 是否成功
  */
  boolean saveBatch(List<FeatureInfoBO> featureInfoList);

  /**
  * 批量更新用例信息
  * @param features 用例信息列表
  * @return 是否成功
  */
  Boolean batchUpdate(List<FeatureInfoBO> features);

}
