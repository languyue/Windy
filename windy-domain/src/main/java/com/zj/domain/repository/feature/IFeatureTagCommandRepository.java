package com.zj.domain.repository.feature;

import com.zj.domain.entity.bo.feature.FeatureTagBO;
import java.util.List;
import com.zj.domain.repository.marker.ConfigCommandRepository;

/**
* @author guyuelan
* @since 2023/5/18
*/
public interface IFeatureTagCommandRepository extends IFeatureTagQueryRepository, ConfigCommandRepository {

  /**
  * 批量保存用例标签
  * @param tagList 标签列表
  * @return 是否成功
  */
  boolean saveBatchTag(List<FeatureTagBO> tagList);

  /**
  * 删除用例标签
  * @param featureId 用例ID
  * @return 是否成功
  */
  boolean deleteByFeatureId(String featureId);

}
