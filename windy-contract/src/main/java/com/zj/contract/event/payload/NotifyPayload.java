package com.zj.contract.event.payload;

/**
 * 结果通知载荷的公共标记。
 *
 * <p>{@code ResultEvent.params} 承载 8 种通知内容，具体类型由信封上的
 * {@code notifyType} 决定，Jackson 按该字段做多态绑定。</p>
 *
 * @author guyuelan
 */
public interface NotifyPayload {
}
