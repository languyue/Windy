package com.zj.client.handler.pipeline.executer.trigger.strategy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zj.client.handler.pipeline.executer.trigger.INodeTrigger;
import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel;
import com.zj.client.handler.pipeline.executer.vo.RefreshContext;
import com.zj.client.handler.pipeline.executer.vo.TaskNode;
import com.zj.common.adapter.invoker.IMasterCallback;
import com.zj.common.entity.dto.NodeRecordStatus;
import com.zj.common.enums.ExecuteType;
import com.zj.contract.enums.ProcessStatus;
import com.zj.contract.json.ContractJson;
import com.zj.contract.pipeline.node.ApprovalContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 审批节点处理
 *
 * @author guyuelan
 * @since 2023/5/8
 */
@Slf4j
@Component
public class ApprovalTrigger implements INodeTrigger<ApprovalContext> {

  private final IMasterCallback masterInvoker;

  public ApprovalTrigger(IMasterCallback masterInvoker) {
    this.masterInvoker = masterInvoker;
  }

  @Override
  public ExecuteType type() {
    return ExecuteType.APPROVAL;
  }

  @Override
  public void triggerRun(ApprovalContext context, TaskNode taskNode) {
    log.info("approval trigger run, no need to do");
    taskNode.setExpireTime(context.getMaxWait() * 1000L);
  }

  @Override
  public QueryResponseModel queryStatus(RefreshContext refreshContext, TaskNode taskNode) {
    //审批结果由用户在ui上确认，console下发审批命令给master，master推进node_record状态，此处只读取状态
    NodeRecordStatus nodeRecord = masterInvoker.getNodeRecordStatus(taskNode.getRecordId());
    log.info("get approval record recordId={} status={}", taskNode.getRecordId(),
        Objects.isNull(nodeRecord) ? null : nodeRecord.getStatus());
    QueryResponseModel responseModel = new QueryResponseModel();
    if (Objects.isNull(nodeRecord)) {
      return responseModel;
    }
    responseModel.setMessage(getMessageList(nodeRecord));
    responseModel.setStatus(nodeRecord.getStatus());
    //审批结果作为断言比较的输入，统一成 Map 结构
    responseModel.setData(ContractJson.convert(nodeRecord, new TypeReference<Map<String, Object>>() { }));
    return responseModel;
  }

  private List<String> getMessageList(NodeRecordStatus nodeRecord) {
    return Optional.ofNullable(nodeRecord.getResult())
        .orElseGet(() -> Collections.singletonList(
            ProcessStatus.exchange(nodeRecord.getStatus()).getDesc()));
  }
}
