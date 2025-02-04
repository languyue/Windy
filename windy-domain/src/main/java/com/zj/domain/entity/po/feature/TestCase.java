package com.zj.domain.entity.po.feature;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2022/12/12
 */
@Data
public class TestCase {

  private Long id;

  /**
   * 测试用例集ID
   * */
  private String testCaseId;

  /**
   * 用例集名称
   * */
  private String testCaseName;

  /**
   * 用例集描述
   * */
  private String description;

  /**
   * 服务ID
   * */
  private String serviceId;

  /**
   * 测试集合类型:
   * 1 普通测试集
   * 2 e2e测试集
   */
  private Integer caseType;

  /**
   * 用户ID
   */
  private String userId;

  /**
   * 创建时间
   */
  private Long createTime;

  /**
   * 修改时间
   */
  private Long updateTime;
}
