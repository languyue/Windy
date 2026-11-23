package com.zj.contract.pipeline.node;

import lombok.Getter;
import lombok.Setter;

/**
 * @author guyuelan
 * @since 2022/5/24
 */
@Getter
@Setter
public class NodeConfig {

  /**
   * 是否跳过错误
   * */
  private boolean ignoreError;
}
