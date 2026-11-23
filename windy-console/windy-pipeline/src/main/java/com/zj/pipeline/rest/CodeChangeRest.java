package com.zj.pipeline.rest;

import com.zj.contract.rpc.PageSize;
import com.zj.contract.exception.ErrorCode;
import com.zj.contract.rpc.ResponseMeta;
import com.zj.domain.entity.bo.pipeline.CodeChangeBO;
import com.zj.domain.entity.bo.pipeline.RelationDemandBug;
import com.zj.pipeline.entity.dto.CodeChangeDto;
import com.zj.pipeline.service.CodeChangeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author guyuelan
 * @since 2021/10/15
 */

@RequestMapping("/v1/devops/pipeline")
@RestController
public class CodeChangeRest {

  private final CodeChangeService codeChangeService;

  public CodeChangeRest(CodeChangeService codeChangeService) {
    this.codeChangeService = codeChangeService;
  }

  @ResponseBody
  @GetMapping("/{serviceId}/change/{codeChangeId}")
  public ResponseMeta<CodeChangeBO> queryCodeChange(@NotNull @PathVariable("codeChangeId") String codeChangeId,
                                                    @NotNull @PathVariable("serviceId") String serviceId) {
    return new ResponseMeta<CodeChangeBO>(ErrorCode.SUCCESS, codeChangeService.getCodeChange(serviceId, codeChangeId));
  }

  @ResponseBody
  @GetMapping("/relations")
  public ResponseMeta<List<RelationDemandBug>> queryRelationIds(@RequestParam("name") String queryName) {
    return new ResponseMeta<List<RelationDemandBug>>(ErrorCode.SUCCESS, codeChangeService.queryRelationIds(queryName));
  }

  @ResponseBody
  @PostMapping("/change")
  public ResponseMeta<String> createCodeChange(@NotNull @Validated @RequestBody CodeChangeDto codeChangeDto) {
    return new ResponseMeta<String>(ErrorCode.SUCCESS, codeChangeService.createCodeChange(codeChangeDto));
  }

  @ResponseBody
  @PutMapping("/{serviceId}/change/{codeChangeId}")
  public ResponseMeta<Boolean> updateCodeChange(@NotNull @PathVariable("serviceId") String serviceId,
                                                @NotNull @PathVariable("codeChangeId") String codeChangeId,
                                                @NotNull @RequestBody CodeChangeDto codeChangeDto) {
    return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, codeChangeService.updateCodeChange(serviceId, codeChangeId, codeChangeDto));
  }

  @ResponseBody
  @GetMapping("/{serviceId}/changes")
  public ResponseMeta<PageSize<CodeChangeBO>> listCodeChanges(@NotNull @PathVariable("serviceId") String serviceId,
                                                              @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                              @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                              @RequestParam(value = "name", defaultValue = "") String name) {
    return new ResponseMeta<PageSize<CodeChangeBO>>(ErrorCode.SUCCESS, codeChangeService.listCodeChanges(serviceId, page, size, name));
  }

  @ResponseBody
  @DeleteMapping("/{serviceId}/change/{codeChangeId}")
  public ResponseMeta<Boolean> deleteCodeChange(@NotNull @PathVariable("serviceId") String serviceId,
                                                @NotNull @PathVariable("codeChangeId") String codeChangeId) {
    return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, codeChangeService.deleteCodeChange(serviceId, codeChangeId));
  }
}
