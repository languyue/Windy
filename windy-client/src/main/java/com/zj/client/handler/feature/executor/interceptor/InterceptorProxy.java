package com.zj.client.handler.feature.executor.interceptor;

import com.zj.contract.feature.exec.ExecutePointVo;
import com.zj.plugin.loader.ExecuteDetailVo;
import com.zj.client.handler.feature.executor.vo.FeatureExecuteContext;
import com.zj.contract.feature.ExecutorUnit;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class InterceptorProxy {

  private List<IExecuteInterceptor> interceptors;

  public InterceptorProxy(List<IExecuteInterceptor> interceptors) {
    this.interceptors = interceptors;
  }


  public void beforeExecute(ExecutorUnit executorUnit, FeatureExecuteContext context) {
    interceptors.forEach(executeInterceptor ->
            executeInterceptor.beforeExecute(executorUnit, context));
  }

  public void afterExecute(ExecutePointVo executePoint, ExecuteDetailVo executeDetailVo,
      FeatureExecuteContext context) {
    interceptors.forEach(executeInterceptor -> executeInterceptor
            .afterExecute(executePoint, executeDetailVo, context));
  }
}
