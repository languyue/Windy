package com.zj.client.config;

import com.zj.common.adapter.pool.WindyThreadPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author guyuelan
 * @since 2023/4/14
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {

  @Bean("pipelinePool")
  public Executor getPipelineExecutor() {
    WindyThreadPool windyThreadPool = new WindyThreadPool();
    windyThreadPool.setCorePoolSize(10);
    windyThreadPool.setMaxPoolSize(30);
    windyThreadPool.setAllowCoreThreadTimeOut(false);
    windyThreadPool.setQueueSize(100);
    windyThreadPool.setThreadNamePrefix("pipeline-thread-");
    return windyThreadPool;
  }

  @Bean("loopQueryPool")
  public Executor getQueryLooperExecutor() {
    WindyThreadPool windyThreadPool = new WindyThreadPool();
    windyThreadPool.setCorePoolSize(20);
    windyThreadPool.setMaxPoolSize(80);
    windyThreadPool.setAllowCoreThreadTimeOut(false);
    windyThreadPool.setQueueSize(100);
    windyThreadPool.setThreadNamePrefix("query-loop-thread-");
    return windyThreadPool;
  }

  @Bean("gitOperatePool")
  public Executor gitOperateExecutor() {
    WindyThreadPool windyThreadPool = new WindyThreadPool();
    windyThreadPool.setTimeout(60 * 60L);
    windyThreadPool.setCorePoolSize(10);
    windyThreadPool.setMaxPoolSize(30);
    windyThreadPool.setAllowCoreThreadTimeOut(false);
    windyThreadPool.setQueueSize(100);
    windyThreadPool.setThreadNamePrefix("buildCode-thread-");
    return windyThreadPool;
  }

  @Bean("eventBusPool")
  public Executor getEventBusPool() {
    WindyThreadPool windyThreadPool = new WindyThreadPool();
    windyThreadPool.setCorePoolSize(10);
    windyThreadPool.setMaxPoolSize(20);
    windyThreadPool.setAllowCoreThreadTimeOut(false);
    windyThreadPool.setQueueSize(100);
    windyThreadPool.setThreadNamePrefix("event-bus-");
    return windyThreadPool;
  }

  @Bean("generatePool")
  public Executor getGeneratePool() {
    WindyThreadPool windyThreadPool = new WindyThreadPool();
    windyThreadPool.setCorePoolSize(10);
    windyThreadPool.setMaxPoolSize(20);
    windyThreadPool.setAllowCoreThreadTimeOut(false);
    windyThreadPool.setQueueSize(100);
    windyThreadPool.setThreadNamePrefix("generate-");
    return windyThreadPool;
  }

  @Bean("featureExecutePool")
  public Executor getFeatureExecutePool() {
    WindyThreadPool windyThreadPool = new WindyThreadPool();
    windyThreadPool.setCorePoolSize(10);
    windyThreadPool.setMaxPoolSize(40);
    windyThreadPool.setAllowCoreThreadTimeOut(false);
    windyThreadPool.setQueueSize(100);
    windyThreadPool.setThreadNamePrefix("feature-");
    return windyThreadPool;
  }

  @Bean("cleanDirtyDataExecutePool")
  public Executor getCleanDirtyDataExecutePool() {
    WindyThreadPool windyThreadPool = new WindyThreadPool();
    windyThreadPool.setCorePoolSize(10);
    windyThreadPool.setMaxPoolSize(40);
    windyThreadPool.setAllowCoreThreadTimeOut(false);
    windyThreadPool.setQueueSize(100);
    windyThreadPool.setThreadNamePrefix("clean-dirty-");
    //清理脏数据处理任务，堆积就直接清除
    windyThreadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
    return windyThreadPool;
  }

  @Bean("asyncExecuteFeaturePool")
  public Executor getAsyncExecuteFeaturePool() {
    WindyThreadPool windyThreadPool = new WindyThreadPool();
    windyThreadPool.setCorePoolSize(10);
    windyThreadPool.setMaxPoolSize(40);
    windyThreadPool.setAllowCoreThreadTimeOut(false);
    windyThreadPool.setQueueSize(1000);
    windyThreadPool.setThreadNamePrefix("async-exe-");
    return windyThreadPool;
  }
}
