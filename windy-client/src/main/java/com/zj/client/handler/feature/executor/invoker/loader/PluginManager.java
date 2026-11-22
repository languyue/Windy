package com.zj.client.handler.feature.executor.invoker.loader;

import com.zj.client.config.GlobalEnvConfig;
import com.zj.common.entity.dto.PluginInfo;
import com.zj.plugin.loader.Feature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author guyuelan
 * @since 2023/7/12
 */
@Slf4j
@Component
public class PluginManager {

  public static final String PLUGIN_PATH = "plugins";
  public static final String PLUGINS_PATH_FORMAT = "file:%s/" + PLUGIN_PATH + "/*.jar";

  private final GlobalEnvConfig globalEnvConfig;

  /**
   * 当前生效的插件类加载器
   * */
  private URLClassLoader currentLoader;

  /**
   * 上一轮的插件类加载器，延迟一轮关闭，避免打断正在执行的用例
   * */
  private URLClassLoader retiredLoader;

  /**
   * 本地jar的指纹缓存，格式为 文件大小-修改时间-md5
   * 轮询周期只有5秒，用文件签名做记忆化，文件没变就不重复计算md5
   * */
  private final Map<String, String> md5Cache = new ConcurrentHashMap<>();

  public PluginManager(GlobalEnvConfig globalEnvConfig) {
    this.globalEnvConfig = globalEnvConfig;
  }

  /**
   * 扫描本地插件目录并加载插件定义
   *
   * @return 插件列表，加载成功但无插件时为空集合；加载失败返回null，调用方需保留原有实例
   * */
  public synchronized List<Feature> loadPlugins() {
    List<Feature> features = new ArrayList<>();
    URLClassLoader newLoader;
    try {
      URL[] urls = getFileUrls();
      newLoader = new URLClassLoader(urls,
          Thread.currentThread().getContextClassLoader());
      ServiceLoader<Feature> serviceLoader = ServiceLoader.load(Feature.class, newLoader);
      for (Feature feature : serviceLoader) {
        features.add(feature);
      }
    } catch (Exception e) {
      log.error("load class error", e);
      return null;
    }

    //延迟一轮关闭，本轮之前的在途执行至少还有一个加载周期的时间收尾
    closeQuietly(retiredLoader);
    retiredLoader = currentLoader;
    currentLoader = newLoader;
    return features;
  }

  private void closeQuietly(URLClassLoader loader) {
    if (Objects.isNull(loader)) {
      return;
    }
    try {
      loader.close();
      log.info("plugin classloader closed");
    } catch (Exception e) {
      log.warn("close plugin classloader error", e);
    }
  }

  /**
   * 本地jar的md5，与主节点下发的hashValue同源(控制台用 md5Hex(jar字节) 生成)
   * 直接比对本地的字节，不依赖进程内存，所以重启后不会重复下载，手工替换jar也能识别
   *
   * @return 文件不存在或读不出时返回null
   * */
  public String getLocalPluginMd5(String pluginName) {
    File jarFile = new File(pluginDir() + File.separator + pluginName);
    if (!jarFile.isFile()) {
      md5Cache.remove(pluginName);
      return null;
    }

    String signature = jarFile.length() + "-" + jarFile.lastModified();
    String cacheValue = md5Cache.get(pluginName);
    if (Objects.nonNull(cacheValue) && cacheValue.startsWith(signature + "-")) {
      return cacheValue.substring(signature.length() + 1);
    }

    try {
      String md5 = DigestUtils.md5DigestAsHex(FileUtils.readFileToByteArray(jarFile));
      md5Cache.put(pluginName, signature + "-" + md5);
      return md5;
    } catch (Exception e) {
      log.error("read local plugin jar error, path={}", jarFile.getPath(), e);
      return null;
    }
  }

  /**
   * 本地插件目录下的jar文件名，直接列目录而不是从URL解析，避免文件名被转义后比对不上
   * */
  public List<String> listLocalPluginNames() {
    File dir = new File(pluginDir());
    String[] names = dir.list((d, name) -> name.endsWith(".jar"));
    if (Objects.isNull(names)) {
      return Collections.emptyList();
    }
    return Arrays.asList(names);
  }

  /**
   * 删除本地已经不在可用列表中的插件，否则重启后会被重新扫描加载
   * */
  public void deleteLocalPlugin(String pluginName) {
    try {
      File jarFile = new File(pluginDir() + File.separator + pluginName);
      FileUtils.deleteQuietly(jarFile);
      md5Cache.remove(pluginName);
      log.warn("delete local plugin jar {}", jarFile.getPath());
    } catch (Exception e) {
      log.error("delete local plugin jar error, name={}", pluginName, e);
    }
  }

  private String pluginDir() {
    return globalEnvConfig.getWorkspace() + File.separator + PLUGIN_PATH;
  }

  public URL[] getFileUrls() throws IOException {
    String path = String.format(PLUGINS_PATH_FORMAT, globalEnvConfig.getWorkspace());
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    Resource[] resources = resolver.getResources(path);
    URL[] urls = new URL[resources.length];
    for (int i = 0; i < resources.length; i++) {
      urls[i] = resources[i].getURL();
    }
    return urls;
  }

  /**
   * 落盘jar字节
   *
   * @return true表示文件已写入，写失败的插件下一轮会因指纹仍不匹配而自动重试
   * */
  public boolean saveJarPlugin(PluginInfo plugin) {
    File jarPath = new File(pluginDir() + File.separator + plugin.getPluginName());
    try {
      FileUtils.createParentDirectories(jarPath);
      FileUtils.writeByteArrayToFile(jarPath, plugin.getFileData());
      //文件已变，作废记忆化的指纹
      md5Cache.remove(plugin.getPluginName());
      return true;
    } catch (Exception e) {
      log.error("save jar file error, path={}", jarPath.getPath(), e);
      return false;
    }
  }
}
