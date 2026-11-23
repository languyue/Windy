package com.zj.master.notify;

import com.zj.contract.enums.NotifyType;
import com.zj.contract.event.ResultEvent;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
public interface INotifyEvent {

  NotifyType type();

  boolean handle(ResultEvent resultEvent);
}
