package com.zj.master.rest;

import com.zj.contract.dispatch.DispatchTaskModel;
import com.zj.contract.dispatch.FeatureDispatchResult;
import com.zj.contract.dispatch.GenerateDispatchResult;
import com.zj.contract.dispatch.PipelineDispatchResult;
import com.zj.contract.dispatch.StopDispatch;
import com.zj.contract.dispatch.StopDispatchResult;
import com.zj.contract.exception.ErrorCode;
import com.zj.contract.rpc.ResponseMeta;
import com.zj.master.service.TaskLogService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author guyuelan
 * @since 2023/5/11
 */
@RestController
@RequestMapping("/v1/devops/dispatch")
public class DispatchTaskRest {

  private final TaskLogService taskLogService;

  public DispatchTaskRest(TaskLogService taskLogService) {
    this.taskLogService = taskLogService;
  }

  /**
   * 用例任务(FEATURE_TASK)与批量执行用例(FEATURE)共用入口，两种流程由请求体的type区分
   */
  @PostMapping("/task/feature")
  public ResponseMeta<FeatureDispatchResult> createFeatureTask(@RequestBody DispatchTaskModel task) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskLogService.createFeatureTask(task));
  }

  /**
   * 流水线任务类型由接口决定，请求体中的type会被覆盖
   */
  @PostMapping("/task/pipeline")
  public ResponseMeta<PipelineDispatchResult> createPipelineTask(@RequestBody DispatchTaskModel task) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskLogService.createPipelineTask(task));
  }

  /**
   * 生成二方包任务类型由接口决定，请求体中的type会被覆盖
   */
  @PostMapping("/task/generate")
  public ResponseMeta<GenerateDispatchResult> createGenerateTask(@RequestBody DispatchTaskModel task) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskLogService.createGenerateTask(task));
  }

  @PostMapping("/stop")
  public ResponseMeta<StopDispatchResult> stopTask(@RequestBody StopDispatch stopDispatch) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskLogService.pauseTask(stopDispatch));
  }
}
