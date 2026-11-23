package com.zj.common.adapter.invoker;

import com.zj.common.entity.event.PipelineFinishedEvent;

/**
 * master 向 console 投递的事件通道。
 *
 * 运行态进程不再直接写配置域与业务域的表，跨域影响一律通过这里通知归属进程自己处理。
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
