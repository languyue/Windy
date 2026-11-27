package com.zj.common.port.codechange;

import java.util.List;

/**
 * 代码变更只读查询端口。
 *
 * <p>由流水线域提供实现，供其他业务域（如需求域）判断某个业务资源是否已绑定
 * 代码分支。调用方只依赖本接口，不依赖流水线域的仓储与领域模型。</p>
 *
 * @author guyuelan
 */
public interface ICodeChangeReader {

  /**
   * 查询关联到指定业务资源的代码变更
   *
   * @param relationId   业务资源ID
   * @param relationType 关联类型，取值见 RelationType
   * @return 代码变更视图列表
   */
  List<CodeChangeView> listByRelation(String relationId, Integer relationType);
}
