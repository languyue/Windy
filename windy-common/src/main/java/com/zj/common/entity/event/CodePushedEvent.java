package com.zj.common.entity.event;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * 代码分支被 push 后的进程内事件。
 *
 * <p>流水线域只宣告"哪些关联资源随本次推送进入研发中"，需求、缺陷的状态怎么变
 * 由业务域自己监听处理，流水线不再跨域写业务域的状态。</p>
 *
 * @author guyuelan
 */
@Data
public class CodePushedEvent implements Serializable {

  /**
   * 推送的服务ID
   */
  private String serviceId;

  /**
   * relationType -> 关联的业务ID列表
   */
  private Map<Integer, List<String>> relationMap;
}
