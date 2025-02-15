package com.zj.domain.entity.bo.feature;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2022/12/12
 */
@Data
public class TestCaseBO {

  private Long id;

  /**
   * 测试用例集ID
   * */
  private String testCaseId;

  /**
   * 服务ID
   * */
  private String serviceId;

  /**
   * 用例集名称
   * */
  private String testCaseName;

  /**
   * 用例集描述
   * */
  private String description;

  /**
   * 测试集合类型:
   * 1 普通测试集
   * 2 e2e测试集
   */
  private Integer caseType;

  /**
   * 创建人ID
   */
  private String userId;

  private Long createTime;

  private Long updateTime;
}
