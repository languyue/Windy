package com.zj.master.rest;

import com.zj.common.entity.command.ApproveCommand;
import com.zj.common.entity.command.PipelineHistoryCommand;
import com.zj.common.entity.command.TaskRecordCommand;
import com.zj.contract.rpc.ResponseMeta;
import com.zj.contract.exception.ErrorCode;
import com.zj.master.service.MasterCommandService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * console 下发的运行态命令入口。
 *
 * @author guyuelan
 */
@RestController
@RequestMapping("/v1/devops/master/command")
public class MasterCommandRest {

  private final MasterCommandService masterCommandService;

  public MasterCommandRest(MasterCommandService masterCommandService) {
    this.masterCommandService = masterCommandService;
  }

  @PostMapping("/approve")
  public ResponseMeta<Boolean> approveNode(@RequestBody ApproveCommand command) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, masterCommandService.approveNode(command));
  }

  @PostMapping("/history")
  public ResponseMeta<String> createPipelineHistory(@RequestBody PipelineHistoryCommand command) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, masterCommandService.createPipelineHistory(command));
  }

  @PostMapping("/task-record")
  public ResponseMeta<String> createTaskRecord(@RequestBody TaskRecordCommand command) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, masterCommandService.createTaskRecord(command));
  }

  @DeleteMapping("/history/{pipelineId}")
  public ResponseMeta<Boolean> deletePipelineHistory(@PathVariable("pipelineId") String pipelineId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, masterCommandService.deletePipelineHistory(pipelineId));
  }

  @PostMapping("/node-record")
  public ResponseMeta<Boolean> deleteNodeRecords(@RequestBody List<String> nodeIds) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, masterCommandService.deleteNodeRecords(nodeIds));
  }

  @DeleteMapping("/task-record/{recordId}")
  public ResponseMeta<Boolean> deleteTaskRecord(@PathVariable("recordId") String recordId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, masterCommandService.deleteTaskRecord(recordId));
  }

  @DeleteMapping("/feature-history/{historyId}")
  public ResponseMeta<Boolean> deleteFeatureHistory(@PathVariable("historyId") String historyId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, masterCommandService.deleteFeatureHistory(historyId));
  }
}
