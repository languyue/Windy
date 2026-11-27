package com.zj.domain.repository.feature;

import com.zj.domain.entity.bo.feature.TestCaseBO;
import java.util.List;
import com.zj.domain.repository.marker.ConfigCommandRepository;

/**
* @author guyuelan
* @since 2023/5/18
*/
public interface ITestCaseCommandRepository extends ITestCaseQueryRepository, ConfigCommandRepository {

  /**
  * 保存测试集
  * @param testCaseBO 测试集信息
  * @return 是否成功
  */
  boolean saveCase(TestCaseBO testCaseBO);

  /**
  * 更新测试集
  * @param testCaseBO 测试集信息
  * @return 是否成功
  */
  Boolean updateCase(TestCaseBO testCaseBO);

  /**
  * 删除测试集
  * @param caseId 测试集ID
  * @return 是否成功
  */
  Boolean deleteTestCase(String caseId);

}
