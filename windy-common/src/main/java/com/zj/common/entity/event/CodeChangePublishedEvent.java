package com.zj.common.entity.event;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * 代码变更所在分支已发布完成的进程内事件。
 *
 * <p>流水线域只宣告"哪些变更随哪些分支发布了"，需求、缺陷、工作项要怎么推进状态
 * 由业务域自己监听处理，流水线不再需要编译期依赖业务域。</p>
 *
 * @author guyuelan
 */
@Data
public class CodeChangePublishedEvent implements Serializable {

  /**
   * 发布的服务ID
   */
  private String serviceId;

  /**
   * relationType -> 关联的业务ID列表
   */
  private Map<Integer, List<String>> relationMap;

  /**
   * 本次发布包含的分支
   */
  private List<String> publishBranches;
}
