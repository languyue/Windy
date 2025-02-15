package com.zj.client.handler.pipeline.executer.trigger.strategy;

import com.alibaba.fastjson.JSON;
import com.zj.client.handler.pipeline.executer.trigger.INodeTrigger;
import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel;
import com.zj.client.handler.pipeline.executer.vo.RefreshContext;
import com.zj.client.handler.pipeline.executer.vo.TaskNode;
import com.zj.client.handler.pipeline.executer.vo.TriggerContext;
import com.zj.client.handler.pipeline.executer.vo.WaitRequestContext;
import com.zj.common.enums.ExecuteType;
import com.zj.common.enums.ProcessStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 等待执行处理
 * @author guyuelan
 * @since 2023/5/8
 */
@Slf4j
@Component
public class WaitTrigger implements INodeTrigger {

  public static final String MESSAGE_TIPS = "等待执行结果";
  private final ConcurrentHashMap<String, CountDownLatch> countDownMap = new ConcurrentHashMap<>();

  @Override
  public ExecuteType type() {
    return ExecuteType.WAIT;
  }

  @Override
  public void triggerRun(TriggerContext triggerContext, TaskNode taskNode) throws IOException {
    log.info("start run wait trigger context={}", JSON.toJSONString(triggerContext));
    WaitRequestContext waitRequestContext = JSON.parseObject(
        JSON.toJSONString(triggerContext.getData()), WaitRequestContext.class);
    CompletableFuture.runAsync(() -> {
      try {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownMap.put(taskNode.getRecordId(), countDownLatch);
        countDownLatch.await(waitRequestContext.getWaitTime(), TimeUnit.SECONDS);
      } catch (InterruptedException ignore) {
      }
    }).whenComplete((consumer,ex) ->{
      CountDownLatch countDownLatch = countDownMap.get(taskNode.getRecordId());
      countDownLatch.countDown();
    });
  }

  @Override
  public QueryResponseModel queryStatus(RefreshContext refreshContext, TaskNode taskNode) {
    QueryResponseModel responseModel = new QueryResponseModel();
    responseModel.setMessage(Collections.singletonList(MESSAGE_TIPS));
    responseModel.setStatus(ProcessStatus.RUNNING.getType());
    CountDownLatch countDownLatch = countDownMap.get(taskNode.getRecordId());
    long count = countDownLatch.getCount();
    if (count > 0) {
      return responseModel;
    }

    log.info("wait task complete recordId={}", taskNode.getRecordId());
    responseModel.setStatus(ProcessStatus.SUCCESS.getType());
    countDownMap.remove(taskNode.getRecordId());
    return responseModel;
  }
}
