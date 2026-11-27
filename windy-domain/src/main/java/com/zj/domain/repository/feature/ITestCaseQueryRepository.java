package com.zj.domain.repository.feature;

import com.zj.contract.rpc.PageSize;
import com.zj.domain.entity.bo.feature.TestCaseBO;
import java.util.List;

/**
* @author guyuelan
* @since 2023/5/18
*/
public interface ITestCaseQueryRepository {

  /**
  * 根据ID获取测试集
  * @param caseId 测试集ID
  * @return 测试集信息
  */
  TestCaseBO getTestCaseById(String caseId);

  /**
  * 获取服务所有测试集列表
  * @param serviceId 服务ID
  * @return 测试集列表
  */
  List<TestCaseBO> getServiceCases(String serviceId);

  /**
  * 分页获取服务下测试集列表
  * @param serviceId 服务ID
  * @param page 页码
  * @param pageSize 每页数量
  * @return 测试集列表
  */
  PageSize<TestCaseBO> getCasePage(String serviceId, Integer page, Integer pageSize);

  /**
  * 获取e2e测试集
  * @return 测试集列表
  */
  PageSize<TestCaseBO> getE2ECasesPage(Integer page, Integer pageSize);

  /**
  * 获取所有e2e测试集
  * @return 测试集列表
  */
  List<TestCaseBO> getE2ECases();

}
