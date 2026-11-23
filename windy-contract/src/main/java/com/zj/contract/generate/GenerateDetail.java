package com.zj.contract.generate;

import lombok.Data;

/**
 * 二方包生成参数，client 生成代码时使用，同时作为生成记录的一部分持久化。
 *
 * @author guyuelan
 */
@Data
public class GenerateDetail {

  /**
   * 打包的包名路径
   * */
  private String packageName;

  /**
   * maven打包的版本
   * */
  private String version;

  /**
   * jar包groupId
   * */
  private String groupId;

  /**
   * jar包artifactId
   * */
  private String artifactId;
}
