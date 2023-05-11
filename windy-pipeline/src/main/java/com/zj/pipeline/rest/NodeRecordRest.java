package com.zj.pipeline.rest;

import com.zj.common.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.pipeline.entity.dto.PipelineHistoryDto;
import com.zj.pipeline.service.NodeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author falcon
 * @since 2023/5/9
 */
@RestController
@RequestMapping("/v1/devops/pipeline")
public class NodeRecordRest {

  @Autowired
  private NodeRecordService nodeRecordService;

  @PutMapping("/node/record")
  public ResponseMeta<Boolean> approvalNode(@RequestParam("historyId") String historyId, @RequestParam("nodeId") String nodeId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, nodeRecordService.approval(historyId, nodeId));
  }
}
