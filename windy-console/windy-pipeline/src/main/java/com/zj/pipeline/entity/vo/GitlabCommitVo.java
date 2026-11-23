package com.zj.pipeline.entity.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/6/27
 */
@Data
public class GitlabCommitVo {

  /**
   * 事件分支
   */
  private String ref;

  /**
   * 事件触发人
   */
  @JsonProperty("user_name")
  private String userName;

  /**
   * 事件类型
   */
  @JsonProperty("object_kind")
  private String eventType;
}
