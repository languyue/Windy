package com.zj.client.handler.pipeline.executer.trigger.strategy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zj.client.handler.pipeline.executer.trigger.INodeTrigger;
import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel;
import com.zj.contract.pipeline.node.RefreshContext;
import com.zj.contract.pipeline.node.TaskNode;
import com.zj.common.enums.ExecuteType;
import com.zj.contract.enums.LogType;
import com.zj.contract.enums.ProcessStatus;
import com.zj.common.exception.ExecuteException;
import com.zj.contract.dispatch.DispatchTaskModel;
import com.zj.contract.json.ContractJson;
import com.zj.contract.pipeline.node.FeatureContext;
import com.zj.contract.rpc.ResponseStatusModel;
import com.zj.contract.rpc.ResponseStatusModel.PercentStatics;
import com.zj.common.adapter.invoker.IMasterCallback;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 用例执行处理
 * @author guyuelan
 * @since 2023/5/9
 */
@Slf4j
@Component
public class FeatureTrigger implements INodeTrigger<FeatureContext> {
  public static final String TASK_TIPS = "pipeline feature task";
  private final IMasterCallback masterInvoker;
  public FeatureTrigger(IMasterCallback masterInvoker) {
    this.masterInvoker = masterInvoker;
  }

  @Override
  public ExecuteType type() {
    return ExecuteType.TEST;
  }

  @Override
  public void triggerRun(FeatureContext context, TaskNode taskNode) {
    log.info("start run feature context={}", ContractJson.write(context));
    String taskId = context.getTaskId();
    DispatchTaskModel task = new DispatchTaskModel();
    task.setSourceIds(Collections.singletonList(taskId));
    task.setSourceName(TASK_TIPS);
    task.setType(LogType.FEATURE_TASK.getType());
    task.setTriggerIds(Collections.singletonList(taskNode.getRecordId()));
    String recordId = masterInvoker.runFeatureTask(task);
    log.info("get TestFeatureInvoker triggerRun recordId= {}",recordId);

    //触发任务执行，将任务的记录Id传递给刷新动作
    if (StringUtils.isBlank(recordId)) {
      throw new ExecuteException("pipeline trigger feature task error");
    }
    taskNode.getRefreshContext().setRecordId(recordId);
  }

  @Override
  public QueryResponseModel queryStatus(RefreshContext refreshContext, TaskNode taskNode) {
    QueryResponseModel queryResponseModel = new QueryResponseModel();
    try {
      log.info("get refresh url ={}", refreshContext.getUrl());
      ResponseStatusModel responseStatusModel = masterInvoker.getFeatureTaskStatus(refreshContext.getRecordId());
      log.info("get TestFeatureInvoker result={}", ContractJson.write(responseStatusModel));
      if (Objects.isNull(responseStatusModel)) {
        queryResponseModel.setStatus(ProcessStatus.FAIL.getType());
        queryResponseModel.setMessage(Collections.singletonList("request http error"));
        return queryResponseModel;
      }

      return toQueryResponse(responseStatusModel);
    } catch (Exception e) {
      log.error("request dispatch task error", e);
      queryResponseModel.setStatus(ProcessStatus.FAIL.getType());
      queryResponseModel.setMessage(getErrorMsg(e));
    }
    return queryResponseModel;
  }

  private QueryResponseModel toQueryResponse(ResponseStatusModel responseStatusModel) {
    QueryResponseModel queryResponseModel = new QueryResponseModel();
    queryResponseModel.setStatus(responseStatusModel.getStatus());
    // 历史报文里 message 是单个字符串，落到 QueryResponseModel 的列表字段时按逗号拆分
    if (StringUtils.isNotBlank(responseStatusModel.getMessage())) {
      queryResponseModel.setMessage(Arrays.asList(responseStatusModel.getMessage().split(",")));
    }
    // 执行进度作为断言比较的输入，统一成 Map 结构
    PercentStatics percentStatics = responseStatusModel.getData();
    if (Objects.nonNull(percentStatics)) {
      queryResponseModel.setData(ContractJson.convert(percentStatics,
          new TypeReference<Map<String, Object>>() { }));
    }
    return queryResponseModel;
  }

  private List<String> getErrorMsg(Exception exception) {
    List<String> msg = new ArrayList<>();
    msg.add("trigger node task error: " + exception.toString());
    for (StackTraceElement element : exception.getStackTrace()) {
      msg.add(element.toString());
    }
    return msg;
  }
}
