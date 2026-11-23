package com.zj.domain.repository.feature;

import com.zj.domain.entity.bo.feature.PluginInfoBO;
import java.util.List;

/**
* @author guyuelan
* @since 2023/7/14
*/
public interface IPluginQueryRepository {

  /**
  * 获取所有插件
  * @return 插件列表
  */
  List<PluginInfoBO> getAllPlugins();

}
