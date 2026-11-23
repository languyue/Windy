package com.zj.domain.repository.pipeline.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.contract.rpc.PageSize;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.pipeline.CodeChangeBO;
import com.zj.domain.entity.po.pipeline.CodeChange;
import com.zj.domain.mapper.pipeline.CodeChangeMapper;
import com.zj.domain.repository.pipeline.ICodeChangeCommandRepository;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
@Slf4j
@Repository
public class CodeChangeRepository extends ServiceImpl<CodeChangeMapper, CodeChange> implements
    ICodeChangeCommandRepository {

  @Override
  public CodeChangeBO getCodeChange(String codeChangeId) {
    CodeChange codeChange = getOne(
        Wrappers.lambdaQuery(CodeChange.class).eq(CodeChange::getChangeId, codeChangeId));
    return OrikaUtil.convert(codeChange, CodeChangeBO.class);
  }

  @Override
  public boolean saveCodeChange(CodeChangeBO codeChange) {
    CodeChange change = OrikaUtil.convert(codeChange, CodeChange.class);
    long dateNow = System.currentTimeMillis();
    change.setCreateTime(dateNow);
    change.setUpdateTime(dateNow);
    return save(change);
  }

  @Override
  public boolean updateCodeChange(CodeChangeBO codeChangeBO) {
    CodeChange codeChange = OrikaUtil.convert(codeChangeBO, CodeChange.class);
    codeChange.setUpdateTime(System.currentTimeMillis());
    return update(codeChange, Wrappers.lambdaUpdate(CodeChange.class)
        .eq(CodeChange::getChangeId, codeChange.getChangeId()));
  }

  @Override
  public List<CodeChangeBO> getServiceChanges(String serviceId) {
    List<CodeChange> codeChanges = list(
        Wrappers.lambdaQuery(CodeChange.class).eq(CodeChange::getServiceId, serviceId));
    return OrikaUtil.convertList(codeChanges, CodeChangeBO.class);
  }

  @Override
  public PageSize<CodeChangeBO> getServiceChangesPage(String serviceId, Integer pageNo, Integer size, String name) {
    IPage<CodeChange> pageObj = new Page<>(pageNo, size);
    LambdaQueryWrapper<CodeChange> queryWrapper = Wrappers.lambdaQuery(CodeChange.class);
    if (StringUtils.isNotBlank(name)) {
      queryWrapper.like(CodeChange::getChangeName, name);
    }
    queryWrapper.orderByDesc(CodeChange::getCreateTime).eq(CodeChange::getServiceId, serviceId);
    IPage<CodeChange> codeChangePage = page(pageObj, queryWrapper);
    PageSize<CodeChangeBO> pageSize = new PageSize<>();
    pageSize.setTotal(codeChangePage.getTotal());
    pageSize.setData(OrikaUtil.convertList(codeChangePage.getRecords(), CodeChangeBO.class));
    return pageSize;
  }

  @Override
  public List<CodeChangeBO> getCodeChangeByRelationId(String relationId, Integer relationType) {
    List<CodeChange> codeChanges = list(
            Wrappers.lambdaQuery(CodeChange.class).eq(CodeChange::getRelationId, relationId)
                    .eq(CodeChange::getRelationType, relationType));
    return OrikaUtil.convertList(codeChanges, CodeChangeBO.class);
  }

  @Override
  public boolean deleteCodeChange(String codeChangeId) {
    return remove(Wrappers.lambdaQuery(CodeChange.class).eq(CodeChange::getChangeId, codeChangeId));
  }

  @Override
  public boolean batchDeleteCodeChange(List<String> codeChangeIds) {
    if (CollectionUtils.isEmpty(codeChangeIds)) {
      log.info("change id list is empty, delete false");
      return false;
    }
    return remove(Wrappers.lambdaQuery(CodeChange.class).in(CodeChange::getChangeId, codeChangeIds));
  }
}
