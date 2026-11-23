package com.zj.common.adapter.invoker;

import com.zj.common.entity.dto.NodeRecordStatus;
import com.zj.contract.dispatch.DispatchTaskModel;
import com.zj.contract.event.ResultEvent;
import com.zj.contract.rpc.PluginInfo;
import com.zj.contract.rpc.ResponseStatusModel;

import java.util.List;

/**
 * client 在执行过程中回调 master 的入口。
 * 与 {@link IMasterApi} 分开声明，避免把两个方向的调用混在同一个接口里。
 */
public interface IMasterCallback {

    /**
     * 执行用例任务
     */
    String runFeatureTask(DispatchTaskModel dispatchTaskModel);

    /**
     * 获取用例任务执行状态
     */
    ResponseStatusModel getFeatureTaskStatus(String taskRecordId);

    /**
     * 获取节点执行记录的状态，流水线节点（含审批节点）依赖该状态判断是否可以继续执行
     *
     * @param recordId 节点执行记录ID
     * @return 返回节点记录状态
     */
    NodeRecordStatus getNodeRecordStatus(String recordId);

    /**
     * 接受来自Client节点的结果通知事件
     *
     * @param resultEvent 通知的结果
     * @return 处理是否成功
     */
    boolean notifyExecuteEvent(ResultEvent resultEvent);

    /**
     * 加载可用的用例插件信息，client会定时轮训当前接口以此来完成每个节点的插件安装
     *
     * @return 返回插件信息
     */
    List<PluginInfo> getAvailablePlugins();
}
