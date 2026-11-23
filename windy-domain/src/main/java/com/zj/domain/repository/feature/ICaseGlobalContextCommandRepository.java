package com.zj.domain.repository.feature;

import com.zj.domain.repository.marker.RuntimeCommandRepository;
import java.util.Map;

/**
 * 用例全局上下文写接口，唯一写 owner 是 windy-master。
 *
 * @author guyuelan
 */
public interface ICaseGlobalContextCommandRepository extends ICaseGlobalContextQueryRepository,
    RuntimeCommandRepository {

  /**
   * 回收一批全局上下文取值，已存在的 key 覆盖，不存在则新增
   *
   * @param caseId 测试集ID
   * @param values paramKey -> value
   * @return 是否成功
   */
  boolean recoverContext(String caseId, Map<String, String> values);

  /**
   * 删除测试集的全部全局上下文
   *
   * @param caseId 测试集ID
   * @return 是否成功
   */
  boolean deleteByCaseId(String caseId);
}
