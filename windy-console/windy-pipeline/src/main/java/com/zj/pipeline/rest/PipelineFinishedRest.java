package com.zj.pipeline.rest;

import com.zj.contract.rpc.ResponseMeta;
import com.zj.common.entity.event.PipelineFinishedEvent;
import com.zj.contract.exception.ErrorCode;
import com.zj.pipeline.service.PipelineFinishService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * master 宣告的流水线结束事件入口。
 *
 * @author guyuelan
 */
@RestController
@RequestMapping("/v1/devops/pipeline")
public class PipelineFinishedRest {

  private final PipelineFinishService pipelineFinishService;

  public PipelineFinishedRest(PipelineFinishService pipelineFinishService) {
    this.pipelineFinishService = pipelineFinishService;
  }

  @PostMapping("/finished")
  public ResponseMeta<Boolean> pipelineFinished(@RequestBody PipelineFinishedEvent event) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, pipelineFinishService.handlePublishFinished(event));
  }
}
