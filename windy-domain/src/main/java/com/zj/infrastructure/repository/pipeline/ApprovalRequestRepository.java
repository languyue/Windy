package com.zj.infrastructure.repository.pipeline;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.pipeline.ApprovalRequestBO;
import com.zj.infrastructure.entity.po.pipeline.ApprovalRequest;
import com.zj.infrastructure.mapper.pipeline.ApprovalRequestMapper;
import com.zj.domain.repository.pipeline.IApprovalRequestCommandRepository;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

/**
 * @author guyuelan
 */
@Slf4j
@Repository
public class ApprovalRequestRepository extends
    ServiceImpl<ApprovalRequestMapper, ApprovalRequest> implements IApprovalRequestCommandRepository {

  @Override
  public boolean submitApproval(ApprovalRequestBO approvalRequest) {
    if (Objects.isNull(approvalRequest) || Objects.isNull(approvalRequest.getRecordId())) {
      log.warn("approval request without recordId, ignore it");
      return false;
    }
    long dateNow = System.currentTimeMillis();
    ApprovalRequest exist = getOne(Wrappers.lambdaQuery(ApprovalRequest.class)
        .eq(ApprovalRequest::getRecordId, approvalRequest.getRecordId()));
    ApprovalRequest record = OrikaUtil.convert(approvalRequest, ApprovalRequest.class);
    if (Objects.isNull(exist)) {
      record.setCreateTime(dateNow);
      record.setUpdateTime(dateNow);
      return save(record);
    }
    record.setId(exist.getId());
    record.setCreateTime(exist.getCreateTime());
    record.setUpdateTime(dateNow);
    return updateById(record);
  }

  @Override
  public boolean deleteByHistoryId(String historyId) {
    return remove(Wrappers.lambdaQuery(ApprovalRequest.class)
        .eq(ApprovalRequest::getHistoryId, historyId));
  }

  @Override
  public ApprovalRequestBO getByRecordId(String recordId) {
    ApprovalRequest record = getOne(Wrappers.lambdaQuery(ApprovalRequest.class)
        .eq(ApprovalRequest::getRecordId, recordId));
    return OrikaUtil.convert(record, ApprovalRequestBO.class);
  }

  @Override
  public List<ApprovalRequestBO> listByHistoryId(String historyId) {
    List<ApprovalRequest> records = list(Wrappers.lambdaQuery(ApprovalRequest.class)
        .eq(ApprovalRequest::getHistoryId, historyId).orderByDesc(ApprovalRequest::getCreateTime));
    if (CollectionUtils.isEmpty(records)) {
      return Collections.emptyList();
    }
    return OrikaUtil.convertList(records, ApprovalRequestBO.class);
  }
}
