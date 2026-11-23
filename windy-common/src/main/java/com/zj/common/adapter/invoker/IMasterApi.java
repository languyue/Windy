package com.zj.common.adapter.invoker;

import com.zj.contract.dispatch.DispatchTaskModel;
import com.zj.contract.dispatch.StopDispatch;
import com.zj.contract.rpc.MasterCollectDto;

import java.util.List;

/**
 * console 向 master 提交任务与查询运行态信息的入口。
 * 与 {@link IMasterCallback} 分开声明，避免把两个方向的调用混在同一个接口里。
 */
public interface IMasterApi {

    /**
     * 执行用例任务
     */
    String runFeatureTask(DispatchTaskModel dispatchTaskModel);

    /**
     * 执行流水线任务
     */
    String startPipelineTask(DispatchTaskModel dispatchTaskModel);

    /**
     * 执行生成二方包任务
     */
    Boolean runGenerateTask(DispatchTaskModel dispatchTaskModel);

    /**
     * 停止执行任务
     *
     * @param stopDispatch 停止任务执行信息
     * @return 返回停止是否成功
     */
    boolean stopDispatch(StopDispatch stopDispatch);

    /**
     * 获取master节点监控状态信息
     *
     * @return 返回监控信息
     */
    List<MasterCollectDto> requestMasterMonitor();
}
