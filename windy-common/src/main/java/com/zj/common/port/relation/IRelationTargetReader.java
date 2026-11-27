package com.zj.common.port.relation;

import java.util.List;

/**
 * 关系目标只读检索端口。
 *
 * <p>由需求域提供实现，供其他业务域（如流水线在做代码变更关联时）按名称检索
 * 需求、缺陷与工作项。调用方只依赖本接口，不依赖需求域的仓储与领域模型。</p>
 *
 * @author guyuelan
 */
public interface IRelationTargetReader {

  /**
   * 按名称模糊检索可关联的需求、缺陷与工作项
   *
   * @param name 名称关键字
   * @return 关系目标视图列表
   */
  List<RelationTargetView> searchByName(String name);
}
