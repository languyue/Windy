package com.zj.client.handler.feature.executor.invoker;


import com.zj.client.handler.feature.executor.vo.FeatureExecuteContext;
import com.zj.common.enums.InvokerType;
import com.zj.common.entity.feature.ExecutorUnit;
import com.zj.plugin.loader.IAsyncNotifyListener;

import java.util.Objects;

public interface IExecuteInvoker {

  InvokerType type();

  Object invoke(ExecutorUnit executorUnit, FeatureExecuteContext featureExecuteContext);

  default Object invoke(ExecutorUnit executorUnit, FeatureExecuteContext featureExecuteContext,
                IAsyncNotifyListener notifyListener){
    if (Objects.isNull(notifyListener)) {
      return invoke(executorUnit, featureExecuteContext);
    }
    return null;
  }
}
