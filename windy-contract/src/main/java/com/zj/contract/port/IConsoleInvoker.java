package com.zj.contract.port;

import com.zj.contract.event.PipelineFinishedEvent;

/**
 * master 向 console 投递的事件通道。
 *
 * 运行态进程不再直接写配置域与业务域的表，跨域影响一律通过这里通知归属进程自己处理。
 *
 * <p>跨 JVM 协议：调用方是 master，实现方是 console 的 PipelineFinishedRest。</p>
 *
 * @author guyuelan
 */
public interface IConsoleInvoker {

    /**
     * 宣告一条流水线执行结束，由 console 决定业务域的后续状态变化
     *
     * @param event 结束事件
     * @return 投递是否成功
     */
    boolean notifyPipelineFinished(PipelineFinishedEvent event);
}
