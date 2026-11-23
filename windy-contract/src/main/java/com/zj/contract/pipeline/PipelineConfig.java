package com.zj.contract.pipeline;

import lombok.Data;

import java.util.Map;

/**
 * @author guyuelan
 * @since 2023/7/19
 */
@Data
public class PipelineConfig {

  /**
   * 流水线定时点
   * */
  private String schedule;

  private Map<String, Object> paramList;
}
