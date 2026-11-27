package com.zj.contract.port;

import com.zj.contract.dispatch.DispatchTaskModel;
import com.zj.contract.dispatch.FeatureDispatchResult;
import com.zj.contract.dispatch.GenerateDispatchResult;
import com.zj.contract.dispatch.PipelineDispatchResult;
import com.zj.contract.dispatch.StopDispatch;
import com.zj.contract.dispatch.StopDispatchResult;
import com.zj.contract.rpc.MasterCollectDto;

import java.util.List;

/**
 * console 向 master 提交任务与查询运行态信息的入口。
 * 与 {@link IMasterCallback} 分开声明，避免把两个方向的调用混在同一个接口里。
 *
 * <p>跨 JVM 协议：调用方是 console，实现方是 master 的 DispatchTaskRest 等。</p>
 */
public interface IMasterApi {

    /**
     * 执行用例、测试集任务
     */
    FeatureDispatchResult runFeatureTask(DispatchTaskModel dispatchTaskModel);

    /**
     * 执行流水线任务
     */
    PipelineDispatchResult startPipelineTask(DispatchTaskModel dispatchTaskModel);

    /**
     * 执行生成二方包任务
     */
    GenerateDispatchResult runGenerateTask(DispatchTaskModel dispatchTaskModel);

    /**
     * 停止执行任务
     *
     * @param stopDispatch 停止任务执行信息
     * @return 停止指令的受理结果
     */
    StopDispatchResult stopDispatch(StopDispatch stopDispatch);

    /**
     * 获取master节点监控状态信息
     *
     * @return 返回监控信息
     */
    List<MasterCollectDto> requestMasterMonitor();
}
