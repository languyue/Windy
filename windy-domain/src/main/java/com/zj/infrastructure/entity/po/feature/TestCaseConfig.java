package com.zj.infrastructure.entity.po.feature;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2022/12/19
 */
@Data
public class TestCaseConfig {

  private Long id;

  private String configId;

  /**
   * 测试集ID
   * */
  private String unionId;

  /**
   * 父节点ID
   * */
  private String parentId;

  /**
   * 节点类型
   * */
  private Integer type;

  /**
   * 参数Key
   * */
  private String paramKey;

  /**
   * 参数类型
   * */
  private String paramType;

  /**
   * 参数值，落库时按文本存储，运行态按 paramType 还原
   * */
  private Object value;

  /**
   * 参数描述
   */
  private String description;

  /**
   * 排序
   * */
  private int sortOrder;

  /**
   * 创建时间
   */
  private Long createTime;

  /**
   * 修改时间
   */
  private Long updateTime;
}
