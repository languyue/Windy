package com.zj.client.handler.feature.executor.interceptor;


import com.zj.contract.feature.exec.ExecutePointVo;
import com.zj.plugin.loader.ExecuteDetailVo;
import com.zj.client.handler.feature.executor.vo.FeatureExecuteContext;
import com.zj.contract.feature.ExecutorUnit;

public interface IExecuteInterceptor {

  default void beforeExecute(ExecutorUnit executorUnit, FeatureExecuteContext context){}

  default void afterExecute(ExecutePointVo executePoint, ExecuteDetailVo executeDetailVo, FeatureExecuteContext context){}

}
