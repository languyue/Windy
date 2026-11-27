package com.zj.common.port.relation;

import lombok.Data;

/**
 * 跨域查询关系目标的最小只读视图。
 *
 * <p>只承载"选关联对象"所需的三个字段，避免把需求/缺陷/工作项的领域模型
 * 暴露给其他业务域。</p>
 *
 * @author guyuelan
 */
@Data
public class RelationTargetView {

  /**
   * 业务资源ID（需求ID/缺陷ID/工作项ID）
   */
  private String relationId;

  /**
   * 关联类型，取值见 RelationType
   */
  private Integer relationType;

  /**
   * 业务资源名称
   */
  private String relationName;
}
