package com.zj.infrastructure.repository.feature;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.feature.FeatureTagBO;
import com.zj.infrastructure.entity.po.feature.FeatureTag;
import com.zj.infrastructure.mapper.feature.FeatureTagMapper;
import com.zj.domain.repository.feature.IFeatureTagCommandRepository;

import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
@Repository
public class FeatureTagRepository extends ServiceImpl<FeatureTagMapper, FeatureTag> implements
    IFeatureTagCommandRepository {

  @Override
  public boolean saveBatchTag(List<FeatureTagBO> tagList) {
    if (CollectionUtils.isEmpty(tagList)) {
      return false;
    }
    List<FeatureTag> featureTags = OrikaUtil.convertList(tagList, FeatureTag.class);
    return saveBatch(featureTags);
  }

  @Override
  public boolean deleteByFeatureId(String featureId) {
    return remove(Wrappers.lambdaQuery(FeatureTag.class).eq(FeatureTag::getFeatureId, featureId));
  }

  @Override
  public List<FeatureTagBO> getFeatureTags(String featureId) {
    List<FeatureTag> featureTags = list(
        Wrappers.lambdaQuery(FeatureTag.class).eq(FeatureTag::getFeatureId, featureId));
    return OrikaUtil.convertList(featureTags, FeatureTagBO.class);
  }

  @Override
  public List<FeatureTagBO> getFeaturesByTag(List<String> tags) {
    if (CollectionUtils.isEmpty(tags)) {
      return Collections.emptyList();
    }
    List<FeatureTag> featureTags = list(
        Wrappers.lambdaQuery(FeatureTag.class).in(FeatureTag::getTagValue, tags));
    return OrikaUtil.convertList(featureTags, FeatureTagBO.class);
  }
}
