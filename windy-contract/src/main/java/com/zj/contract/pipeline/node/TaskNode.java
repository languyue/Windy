package com.zj.contract.pipeline.node;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2022/5/23
 */
@Data
public class TaskNode extends BaseDispatch{
  /**
   * 流水线节点Id
   * */
  private String nodeId;

  /**
   * 服务Id
   * */
  private String serviceId;

  /**
   * 流水线节点名称
   * */
  private String name;

  /**
   * 运行记录Id
   * */
  private String recordId;

  /**
   * 流水线历史记录Id
   * */
  private String historyId;

  /**
   * 执行类型，默认Http
   * */
  private String executeType;

  /**
   * 触发节点执行的第三方接口信息
   * */
  private RequestContext requestContext;

  /**
   * 刷新节点任务状态的第三方接口信息
   * */
  private RefreshContext refreshContext;

  /**
   * 节点运行配置信息
   * */
  private NodeConfig nodeConfig;

  /**
   * 任务开始执行的时间点
   * */
  private Long executeTime;

  private String logId;

  /**
   * 是否指定单个 client 节点执行，不用负载均衡。git 操作的文件处理需要在同一个节点完成，
   * 所以添加这个标志（只针对 git 相关的流水线子节点，其他类型子节点不受影响）。
   * */
  private boolean requestSingle;

  /**
   * 和 requestSingle 一起使用，指定具体是哪一个 client 节点
   * */
  private String singleClientIp;
}
