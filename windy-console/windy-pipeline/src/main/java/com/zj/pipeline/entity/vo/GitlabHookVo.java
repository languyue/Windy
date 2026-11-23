package com.zj.pipeline.entity.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/6/27
 */
@Data
public class GitlabHookVo {

  private String ref;

  private String project;

  private String message;

  @JsonProperty("commit_id")
  private String commitId;

  @JsonProperty("event_type")
  private String eventType;
}
