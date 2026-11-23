package com.zj.client.handler.feature.executor.invoker.invoke;

import com.zj.client.handler.feature.executor.invoker.IExecuteInvoker;
import com.zj.client.handler.feature.executor.invoker.loader.PluginManager;
import com.zj.client.handler.feature.executor.vo.FeatureExecuteContext;
import com.zj.client.utils.ExceptionUtils;
import com.zj.contract.feature.ExecutorUnit;
import com.zj.contract.support.ParamValueCaster;
import com.zj.common.enums.InvokerType;
import com.zj.plugin.loader.ExecuteDetailVo;
import com.zj.plugin.loader.Feature;
import com.zj.plugin.loader.FeatureDefine;
import com.zj.plugin.loader.IAsyncNotifyListener;
import com.zj.plugin.loader.ParameterDefine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class MethodInvoke implements IExecuteInvoker {

    /**
     * 执行类与实例的缓存，key统一为执行类全名
     * 插件重载时整体替换引用，读侧取局部变量快照，避免重建窗口期读不到实例
     * */
    private volatile Map<String, Object> instanceMap = new ConcurrentHashMap<>();

    /**
     * 记录来源于插件的key，重载时只清理插件部分，保留Class.forName兜底缓存
     * */
    private volatile Set<String> pluginSources = Collections.emptySet();

    private final PluginManager pluginManager;

    public MethodInvoke(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
        new Thread(this::loadPluginFromDisk).start();
    }

    /**
     * 重新扫描本地插件，以插件定义的最新实例覆盖缓存
     *
     * @return true表示实例表已按最新插件刷新
     * */
    public synchronized boolean loadPluginFromDisk() {
        List<Feature> features = pluginManager.loadPlugins();
        if (Objects.isNull(features)) {
            //加载失败时保留原有实例，不能把可用插件一起清掉
            log.warn("load plugin from disk failed, keep the current instance map");
            return false;
        }

        Map<String, Object> newMap = new ConcurrentHashMap<>(instanceMap);
        //先丢弃上一轮来自插件的实例，已下线的插件不会残留在内存中
        newMap.keySet().removeAll(pluginSources);
        Set<String> newSources = new HashSet<>();

        features.forEach(feature -> {
            List<FeatureDefine> featureDefines = feature.scanFeatureDefines();
            if (CollectionUtils.isEmpty(featureDefines)) {
                return;
            }

            featureDefines.forEach(featureDefine -> {
                String source = featureDefine.getSource();
                //ConcurrentHashMap不允许null键
                if (StringUtils.isBlank(source)) {
                    log.warn("feature define source is blank, skip it, className={}",
                            feature.getClass().getSimpleName());
                    return;
                }
                //直接覆盖，同名插件的新版本才能生效
                newMap.put(source, feature);
                newSources.add(source);
                log.info("load plugin instance source={} className={}", source,
                        feature.getClass().getName());
            });
        });

        instanceMap = newMap;
        pluginSources = newSources;
        log.info("plugin instance map refreshed, plugin size={}", newSources.size());
        return true;
    }

    @Override
    public InvokerType type() {
        return InvokerType.METHOD;
    }

    public Object invoke(ExecutorUnit executorUnit, FeatureExecuteContext featureExecuteContext) {
        return invoke(executorUnit, featureExecuteContext, null);
    }

    @Override
    public Object invoke(ExecutorUnit executorUnit, FeatureExecuteContext featureExecuteContext,
                         IAsyncNotifyListener notifyListener) {
        try {
            Object[] objects = null;
            List<ParameterDefine> paramDefines = executorUnit.getParams();
            if (!CollectionUtils.isEmpty(paramDefines)) {
                int i = 0;
                objects = new Object[paramDefines.size()];
                for (ParameterDefine paramDefine : paramDefines) {
                    objects[i] = convertDataToType(paramDefine);
                    i++;
                }
            }

            String service = executorUnit.getService();
            //取一次引用快照，避免与插件重载的整体替换竞争
            Map<String, Object> instances = instanceMap;
            Object instance = StringUtils.isBlank(service) ? null : instances.get(service);
            if (Objects.isNull(instance)) {
                Class<?> cls = Class.forName(service);
                instance = ConstructorUtils.invokeConstructor(cls);
                //key必须与上面的读取保持一致使用类名，之前用name会导致缓存永不命中
                Object exist = instanceMap.putIfAbsent(service, instance);
                if (Objects.nonNull(exist)) {
                    instance = exist;
                }
            }
            bindListenerIfNeed(notifyListener, instance);
            log.info("start invoke obj = {}", instance.getClass().getName());
            return MethodUtils.invokeMethod(instance, executorUnit.getMethod(), objects);
        } catch (Exception e) {
            log.error("invoke method error", e);
            String stringBuilder =
                    "invoke[ " + executorUnit.getService() + " ] method [ " + executorUnit.getMethod() + " ]\r\n" +
                            "\tat" + ExceptionUtils.getSimplifyError(e);
            ExecuteDetailVo executeDetailVo = new ExecuteDetailVo();
            executeDetailVo.setStatus(false);
            executeDetailVo.setErrorMessage(stringBuilder);
            if (Objects.nonNull(featureExecuteContext.getCountDownLatch())) {
                //如果异步用例执行失败，则将当前的异步用例等待直接释放
                featureExecuteContext.getCountDownLatch().countDown();
            }
            return executeDetailVo;
        }
    }

    private void bindListenerIfNeed(IAsyncNotifyListener notifyListener, Object instance) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (Objects.isNull(notifyListener)) {
            return;
        }
        String bindMethodName = "bindListener";
        Method method = MethodUtils.getMatchingMethod(instance.getClass(), bindMethodName, IAsyncNotifyListener.class);
        if (Objects.nonNull(method)) {
            MethodUtils.invokeMethod(instance, bindMethodName, notifyListener);
        }
    }

    /**
     * 插件参数在配置里存的是文本，交给 {@link ParamValueCaster} 按声明类型还原
     */
    public static Object convertDataToType(ParameterDefine paramDefine) {
        if (Objects.isNull(paramDefine.getValue())) {
            return null;
        }
        return ParamValueCaster.cast(paramDefine.getValue(), paramDefine.getType());
    }
}
