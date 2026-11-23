package com.zj.client.handler.pipeline.executer.trigger.strategy;

import com.zj.client.entity.dto.CodeBuildParamDto;
import com.zj.client.handler.pipeline.executer.trigger.INodeTrigger;
import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel;
import com.zj.contract.pipeline.node.RefreshContext;
import com.zj.contract.pipeline.node.TaskNode;
import com.zj.client.service.CodeBuildService;
import com.zj.common.enums.ExecuteType;
import com.zj.common.utils.OrikaUtil;
import com.zj.contract.json.ContractJson;
import com.zj.contract.pipeline.node.BuildCodeContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 代码构建处理
 * @author guyuelan
 * @since 2023/5/8
 */
@Slf4j
@Component
public class BuildCodeTrigger implements INodeTrigger<BuildCodeContext> {

  private final CodeBuildService codeBuildService;

  public BuildCodeTrigger(CodeBuildService codeBuildService) {
    this.codeBuildService = codeBuildService;
  }

  @Override
  public ExecuteType type() {
    return ExecuteType.BUILD;
  }

  @Override
  public void triggerRun(BuildCodeContext context, TaskNode taskNode) throws Exception {
    log.info("start build code context = {}", ContractJson.write(context));
    CodeBuildParamDto codeBuildParamDto = toBuildParam(context, taskNode.getRecordId());
    codeBuildService.buildCode(codeBuildParamDto, taskNode);
  }

  private static CodeBuildParamDto toBuildParam(BuildCodeContext context, String recordId) {
    CodeBuildParamDto codeBuildParamDto = OrikaUtil.convert(context, CodeBuildParamDto.class);
    codeBuildParamDto.setRecordId(recordId);
    // 属性名不同（isPublish / publish），历史上靠 JSON 转换时从未生效过，
    // 发布流水线一直走"拉取第一个绑定分支"的路径，这里保持原行为
    codeBuildParamDto.setPublish(false);
    return codeBuildParamDto;
  }

  @Override
  public QueryResponseModel queryStatus(RefreshContext refreshContext, TaskNode taskNode) {
    return codeBuildService.getRecordStatus(taskNode.getRecordId());
  }
}
