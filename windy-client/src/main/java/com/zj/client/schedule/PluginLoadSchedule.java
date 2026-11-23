package com.zj.client.schedule;

import com.zj.client.handler.feature.executor.invoker.invoke.MethodInvoke;
import com.zj.client.handler.feature.executor.invoker.loader.PluginManager;
import com.zj.common.adapter.invoker.IMasterCallback;
import com.zj.common.adapter.monitor.InstanceMonitor;
import com.zj.contract.rpc.PluginInfo;
import com.zj.common.utils.TraceUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/7/17
 */
@Slf4j
@Component
public class PluginLoadSchedule {

  private final IMasterCallback masterInvoker;
  private final PluginManager pluginManager;
  private final InstanceMonitor instanceMonitor;
  private final MethodInvoke methodInvoke;

  public PluginLoadSchedule(IMasterCallback masterInvoker, PluginManager pluginManager,
      InstanceMonitor instanceMonitor, MethodInvoke methodInvoke) {
    this.masterInvoker = masterInvoker;
    this.pluginManager = pluginManager;
    this.instanceMonitor = instanceMonitor;
    this.methodInvoke = methodInvoke;
  }

  @Scheduled(cron = "0/5 * * * * ? ")
  public void loadSchedule() {
    if (instanceMonitor.isUnStable()) {
      log.info("server is unstable");
      return;
    }

    initTraceId();
    List<PluginInfo> plugins = masterInvoker.getAvailablePlugins();
    if (CollectionUtils.isEmpty(plugins)) {
      log.debug("get plugin list is empty");
      return;
    }

    //本地jar名直接列目录获取，避免文件名被转义后与主节点返回的名称比对不上
    Set<String> localNames = new HashSet<>(pluginManager.listLocalPluginNames());
    List<PluginInfo> needLoad = plugins.stream()
        .filter(plugin -> isPluginChanged(plugin, localNames))
        .collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(needLoad)) {
      log.info("find plugin to load={}", needLoad.stream().map(PluginInfo::getPluginName).collect(
          Collectors.toList()));
      List<PluginInfo> savedList = new ArrayList<>();
      needLoad.forEach(plugin -> {
        if (pluginManager.saveJarPlugin(plugin)) {
          savedList.add(plugin);
        }
      });
      if (savedList.size() < needLoad.size()) {
        log.error("part plugin save failed, saved={}, total={}", savedList.size(),
            needLoad.size());
      }
      //全部写盘失败就不必折腾类加载器，下一轮凭指纹自动重试
      if (CollectionUtils.isNotEmpty(savedList)) {
        methodInvoke.loadPluginFromDisk();
      }
    }

    cleanDeletedPlugins(plugins, localNames);
  }

  /**
   * 同名插件升级后jar名不变，只有内容指纹会变，所以必须比内容而不是比文件名
   * */
  private boolean isPluginChanged(PluginInfo plugin, Set<String> localNames) {
    String name = plugin.getPluginName();
    if (StringUtils.isBlank(name)) {
      log.warn("get a plugin without name, skip it");
      return false;
    }
    if (!localNames.contains(name)) {
      //本地没有这个jar，必须落地
      return true;
    }

    String remoteHash = StringUtils.trimToEmpty(plugin.getHashValue());
    if (StringUtils.isBlank(remoteHash)) {
      //主节点没给指纹时退化为按文件名判断，避免每轮重复下载
      log.warn("plugin {} has no hash value, only check the file name", name);
      return false;
    }

    String localHash = pluginManager.getLocalPluginMd5(name);
    //本地读不到或内容不同，都要重新落地
    return Objects.isNull(localHash) || !remoteHash.equalsIgnoreCase(localHash);
  }

  /**
   * 主节点已不再返回的插件，本地jar也要删除，否则重启后会被重新扫描加载
   * */
  private void cleanDeletedPlugins(List<PluginInfo> plugins, Set<String> localNames) {
    Set<String> remoteNames = plugins.stream().map(PluginInfo::getPluginName)
        .filter(StringUtils::isNotBlank).collect(Collectors.toSet());
    List<String> deleted = localNames.stream()
        .filter(name -> !remoteNames.contains(name)).collect(Collectors.toList());
    if (CollectionUtils.isEmpty(deleted)) {
      return;
    }

    deleted.forEach(name -> {
      log.warn("plugin {} is not in available plugins, remove it from this client", name);
      pluginManager.deleteLocalPlugin(name);
    });
    methodInvoke.loadPluginFromDisk();
  }

  private static void initTraceId() {
    TraceUtils.putTrace(UUID.randomUUID().toString().replace("-",""));
  }
}
