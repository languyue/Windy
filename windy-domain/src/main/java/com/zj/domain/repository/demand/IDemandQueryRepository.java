package com.zj.domain.repository.demand;

import com.zj.contract.rpc.PageSize;
import com.zj.domain.entity.bo.demand.DemandBO;
import com.zj.domain.entity.bo.demand.DemandQueryBO;
import java.util.List;


/**
 * @author guyuelan
 */
public interface IDemandQueryRepository {

  /**
  * 分页获取需求列表
  * @param demandQueryBO 查询条件
  * @return 需求列表
  */
  PageSize<DemandBO> getDemandPage(DemandQueryBO demandQueryBO);

  /**
  * 获取需求详情
  * @param demandId 需求ID
  * @return 需求信息
  */
  DemandBO getDemand(String demandId);

  /**
  * 获取迭代需求
  * @param iterationId 迭代ID
  * @return 需求列表
  */
  List<DemandBO> getIterationDemand(String iterationId);

  /**
  * 模糊查询需求列表
  * @param queryName 查询名称
  * @return 需求列表
  */
  List<DemandBO> getDemandsByFuzzName(String queryName);

  /**
  * 获取空间未处理需求
  * @param spaceId 空间ID
  * @return 需求列表
  */
  List<DemandBO> getSpaceNotHandleDemands(String spaceId);

  /**
  * 获取迭代未处理需求
  * @param iterationId 迭代ID
  * @return 需求列表
  */
  List<DemandBO> getIterationNotHandleDemands(String iterationId);

  /**
  * 获取未完成需求
  * @param demandIds 需求ID列表
  * @return 需求列表
  */
  List<DemandBO> getNotCompleteDemandByIds(List<String> demandIds);

  /**
  * 查询所有的需求列表
  * @return 返回需求列表
  */
  List<DemandBO> getAllDemands();

  /**
  * 获取未完成的需求列表
  * @return 返回需求列表
  */
  List<DemandBO> getNotCompleteDemands();

}
