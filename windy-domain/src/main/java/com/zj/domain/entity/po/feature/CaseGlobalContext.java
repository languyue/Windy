package com.zj.domain.entity.po.feature;

import lombok.Data;

/**
 * 用例全局上下文的一次运行期取值。
 *
 * 过去 master 把执行过程中回收的全局变量直接回写 test_case_config.value，
 * 让配置域的表承载了运行态状态；本表把这部分状态独立出来，写 owner 是 master。
 *
 * @author guyuelan
 */
@Data
public class CaseGlobalContext {

  private Long id;

  /**
   * 测试集ID
   * */
  private String unionId;

  /**
   * 全局上下文参数Key
   * */
  private String paramKey;

  /**
   * 全局上下文参数值
   * */
  private String paramValue;

  private Long createTime;

  private Long updateTime;
}
