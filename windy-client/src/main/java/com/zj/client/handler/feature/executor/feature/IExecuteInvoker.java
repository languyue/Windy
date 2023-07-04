package com.zj.client.handler.feature.executor.feature;


import com.zj.client.handler.feature.executor.feature.invoke.InvokerType;
import com.zj.client.handler.feature.executor.vo.ExecutorUnit;

public interface IExecuteInvoker {

  InvokerType type();

  Object invoke(ExecutorUnit executorUnit);
}
