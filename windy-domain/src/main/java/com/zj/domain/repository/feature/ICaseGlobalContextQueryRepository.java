package com.zj.domain.repository.feature;

import java.util.Map;

/**
 * 用例全局上下文只读视图。
 *
 * @author guyuelan
 */
public interface ICaseGlobalContextQueryRepository {

  /**
   * 获取测试集已经回收的全局上下文取值
   *
   * @param caseId 测试集ID
   * @return paramKey -> value，没有则返回空 Map
   */
  Map<String, String> getContextValues(String caseId);
}
