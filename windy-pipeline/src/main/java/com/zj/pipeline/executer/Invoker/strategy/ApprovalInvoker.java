package com.zj.pipeline.executer.Invoker.strategy;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zj.domain.entity.po.pipeline.NodeRecord;
import com.zj.pipeline.executer.Invoker.IRemoteInvoker;
import com.zj.pipeline.executer.vo.ExecuteType;
import com.zj.pipeline.executer.vo.QueryResponseModel;
import com.zj.pipeline.executer.vo.RefreshContext;
import com.zj.pipeline.executer.vo.RequestContext;
import com.zj.pipeline.service.NodeRecordService;
import java.io.IOException;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/5/8
 */
@Slf4j
@Component
public class ApprovalInvoker implements IRemoteInvoker {

  public static final String MESSAGE_TIPS = "审批结果";
  @Autowired
  private NodeRecordService recordService;

  @Override
  public ExecuteType type() {
    return ExecuteType.APPROVAL;
  }

  @Override
  public boolean triggerRun(RequestContext requestContext, String recordId) throws IOException {
    return true;
  }

  @Override
  public String queryStatus(RefreshContext refreshContext, String recordId) {
    //审批通过就直接根据数据库的状态即可，因为这个状态变化不在节点执行是用户在ui界面完成
    NodeRecord record = recordService.getOne(
        Wrappers.lambdaQuery(NodeRecord.class).eq(NodeRecord::getRecordId, recordId));
    log.info("get approval record recordId ={} status={}", recordId, record.getStatus());
    QueryResponseModel responseModel = new QueryResponseModel();
    responseModel.setMessage(Collections.singletonList(MESSAGE_TIPS));
    responseModel.setStatus(record.getStatus());
    return JSON.toJSONString(responseModel);
  }
}
