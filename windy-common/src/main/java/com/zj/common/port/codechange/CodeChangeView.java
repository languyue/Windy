package com.zj.common.port.codechange;

import lombok.Data;

/**
 * 代码变更的最小只读视图。
 *
 * <p>只暴露其他业务域判断关联关系必需的字段，不携带流水线域的完整领域模型。</p>
 *
 * @author guyuelan
 */
@Data
public class CodeChangeView {

  /**
   * 代码变更ID
   */
  private String changeId;

  /**
   * 变更所属服务ID
   */
  private String serviceId;

  /**
   * 变更分支
   */
  private String changeBranch;

  /**
   * 关联类型，取值见 RelationType
   */
  private Integer relationType;

  /**
   * 关联的业务资源ID
   */
  private String relationId;
}
