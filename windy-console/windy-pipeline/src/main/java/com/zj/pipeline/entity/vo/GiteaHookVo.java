package com.zj.pipeline.entity.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/6/27
 */
@Data
public class GiteaHookVo {

  private String ref;

  private Repository repository;

  private Commit commit;

  @JsonProperty("event_type")
  private String eventType;


  @Data
  public static class Repository{
    private String name;
  }

  @Data
  public static class Commit{

    private String message;

    private String id;
  }
}
