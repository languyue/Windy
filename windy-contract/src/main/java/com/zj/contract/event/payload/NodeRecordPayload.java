package com.zj.contract.event.payload;

import java.util.List;
import lombok.Data;

/**
 * 流水线节点执行记录的通知载荷。
 *
 * @author guyuelan
 */
@Data
public class NodeRecordPayload implements NotifyPayload {

  /**
   * 节点ID
   */
  private String nodeId;

  /**
   * 节点执行记录ID
   */
  private String recordId;

  /**
   * 流水线历史ID
   */
  private String historyId;

  private Integer code;

  /**
   * 节点执行过程描述
   */
  private List<String> result;

  /**
   * 执行状态
   */
  private Integer status;

  private Long createTime;

  private Long updateTime;
}
