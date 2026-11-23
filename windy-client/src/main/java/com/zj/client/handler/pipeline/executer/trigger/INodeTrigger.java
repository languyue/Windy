package com.zj.client.handler.pipeline.executer.trigger;


import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel;
import com.zj.client.handler.pipeline.executer.vo.RefreshContext;
import com.zj.client.handler.pipeline.executer.vo.TaskNode;
import com.zj.common.enums.ExecuteType;
import com.zj.contract.pipeline.node.RequestContext;

/**
 * @author guyuelan
 * @since 2022/5/25
 */
public interface INodeTrigger<T extends RequestContext> {

  ExecuteType type();

  /**
   * 触发执行节点任务
   * @param context 节点执行上下文，具体类型由 {@link RequestContext#getType()} 决定
   * @param taskNode 任务
   * */
  void triggerRun(T context, TaskNode taskNode) throws Exception;

  /**
   * 查询节点任务执行状态
   * @param refreshContext  请求刷新状态的参数
   * @param taskNode 任务详情
   * */
  QueryResponseModel queryStatus(RefreshContext refreshContext, TaskNode taskNode);
}
