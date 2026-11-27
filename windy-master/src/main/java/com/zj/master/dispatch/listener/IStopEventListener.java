package com.zj.master.dispatch.listener;

import com.zj.contract.enums.LogType;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
public interface IStopEventListener {

  /**
   * 监听器支持停止的任务类型
   * */
  LogType supportType();

  void stopEvent(InternalEvent event);
}
