package com.zj.domain.repository.feature;

import com.zj.domain.entity.bo.feature.TestCaseConfigBO;
import java.util.List;

/**
* @author guyuelan
* @since 2023/5/18
*/
public interface ITestCaseConfigQueryRepository {

  /**
  * 获取测试集配置
  * @param caseId 测试集ID
  * @return 配置列表
  */
  List<TestCaseConfigBO> getCaseConfigs(String caseId);

}
