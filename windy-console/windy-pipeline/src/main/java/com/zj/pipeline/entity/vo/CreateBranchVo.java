package com.zj.pipeline.entity.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/3/10
 */
@Data
public class CreateBranchVo {

  /**
   * 新分支名称
   * */
  @JsonProperty("new_branch_name")
  private String branchName;
}
