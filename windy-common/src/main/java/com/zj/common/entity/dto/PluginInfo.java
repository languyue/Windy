package com.zj.common.entity.dto;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/7/17
 */
@Data
public class PluginInfo {

  private String pluginName;

  private byte[] fileData;

  /**
   * 插件文件内容指纹，master从数据库直接返回，客户端据此判断同名插件是否发生了变更
   * */
  private String hashValue;
}
