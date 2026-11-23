package com.zj.domain.entity.bo.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zj.contract.pipeline.EnvAccessParams;
import com.zj.domain.entity.enums.EnvType;
import lombok.Data;

@Data
public class DeployEnvironmentBO {

  /**
   * 环境Id
   * */
  private String envId;

  /**
   * 环境名称
   * */
  private String envName;

  /**
   * 环境状态
   * */
  private Integer envStatus;


  /**
   * 环境类型 {@link EnvType}
   * */
  private Integer envType;

  /**
   * 环境执行的必要参数，具体类型由 {@link #envType} 决定
   * */
  @JsonProperty("envParams")
  private EnvAccessParams accessParams;

  /**
   * 创建时间
   * */
  private Long createTime;

  /**
   * 修改时间
   * */
  private Long updateTime;

}
