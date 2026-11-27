package com.zj.infrastructure.repository.feature;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.infrastructure.entity.po.feature.CaseGlobalContext;
import com.zj.infrastructure.mapper.feature.CaseGlobalContextMapper;
import com.zj.domain.repository.feature.ICaseGlobalContextCommandRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

/**
 * @author guyuelan
 */
@Slf4j
@Repository
public class CaseGlobalContextRepository extends
    ServiceImpl<CaseGlobalContextMapper, CaseGlobalContext> implements
    ICaseGlobalContextCommandRepository {

  @Override
  public Map<String, String> getContextValues(String caseId) {
    if (StringUtils.isBlank(caseId)) {
      return Collections.emptyMap();
    }
    List<CaseGlobalContext> contexts = list(Wrappers.lambdaQuery(CaseGlobalContext.class)
        .eq(CaseGlobalContext::getUnionId, caseId));
    return contexts.stream().collect(Collectors.toMap(CaseGlobalContext::getParamKey,
        context -> Optional.ofNullable(context.getParamValue()).orElse(""), (older, latest) -> latest));
  }

  @Override
  public boolean recoverContext(String caseId, Map<String, String> values) {
    if (StringUtils.isBlank(caseId) || MapUtils.isEmpty(values)) {
      return false;
    }
    Map<String, CaseGlobalContext> existMap = list(Wrappers.lambdaQuery(CaseGlobalContext.class)
        .eq(CaseGlobalContext::getUnionId, caseId)).stream()
        .collect(Collectors.toMap(CaseGlobalContext::getParamKey, Function.identity(),
            (older, latest) -> latest));

    long dateNow = System.currentTimeMillis();
    List<CaseGlobalContext> createList = new ArrayList<>();
    List<CaseGlobalContext> updateList = new ArrayList<>();
    values.forEach((paramKey, paramValue) -> {
      CaseGlobalContext exist = existMap.get(paramKey);
      if (Objects.nonNull(exist)) {
        if (Objects.equals(exist.getParamValue(), paramValue)) {
          return;
        }
        exist.setParamValue(paramValue);
        exist.setUpdateTime(dateNow);
        updateList.add(exist);
        return;
      }
      CaseGlobalContext created = new CaseGlobalContext();
      created.setUnionId(caseId);
      created.setParamKey(paramKey);
      created.setParamValue(paramValue);
      created.setCreateTime(dateNow);
      created.setUpdateTime(dateNow);
      createList.add(created);
    });

    boolean createResult = CollectionUtils.isEmpty(createList) || saveBatch(createList);
    boolean updateResult = CollectionUtils.isEmpty(updateList) || updateBatchById(updateList);
    log.info("recover global context caseId={} create={} update={}", caseId, createList.size(),
        updateList.size());
    return createResult && updateResult;
  }

  @Override
  public boolean deleteByCaseId(String caseId) {
    return remove(Wrappers.lambdaQuery(CaseGlobalContext.class)
        .eq(CaseGlobalContext::getUnionId, caseId));
  }
}
