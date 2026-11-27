package com.zj.contract.rpc;

import lombok.Data;

import java.util.List;

/**
 * 节点执行记录的状态视图。
 *
 * <p>master 是 node_record 的唯一写入方，client 通过既有的状态查询通道读取该视图来
 * 判断节点（含审批节点）是否可以继续推进，不再有独立的审批查询接口。</p>
 *
 * @author guyuelan
 */
@Data
public class NodeRecordStatus {

  /**
   * 节点执行记录ID
   */
  private String recordId;

  /**
   * 执行状态，取值见 ProcessStatus
   */
  private Integer status;

  /**
   * 执行过程描述
   */
  private List<String> result;
}
