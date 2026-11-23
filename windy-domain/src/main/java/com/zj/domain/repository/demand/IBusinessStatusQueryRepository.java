package com.zj.domain.repository.demand;

import com.zj.domain.entity.bo.demand.BusinessStatusBO;
import java.util.List;


/**
 * @author guyuelan
 */
public interface IBusinessStatusQueryRepository {

  /**
  * 获取需求状态列表
  * @return 需求状态列表
  */
  List<BusinessStatusBO> getDemandStatuses();

  /**
  * 获取迭代状态列表
  * @return 迭代状态列表
  */
  List<BusinessStatusBO> getIterationStatuses();

  /**
  * 获取缺陷状态列表
  * @return 工作任务状态列表
  */
  List<BusinessStatusBO> getBugStatuses();

  /**
  * 获取工作任务状态列表
  * @return 工作任务状态列表
  */
  List<BusinessStatusBO> getWorkTaskStatuses();

  /**
  * 获取需求标签
  * @return 需求标签
  */
  List<BusinessStatusBO> getDemandTags();

  /**
  * 获取缺陷标签
  * @return 缺陷标签
  */
  List<BusinessStatusBO> getBugTags();

  /**
  * 指定业务类型下的状态是否不可更改
  * @return 是否不可更改
  */
  boolean isUnchangeableStatus(Integer status, String businessType);

  /**
  * 获取客户价值标签列表
  * @return 返回标签字典列表
  */
  List<BusinessStatusBO> getCustomerValues();

}
