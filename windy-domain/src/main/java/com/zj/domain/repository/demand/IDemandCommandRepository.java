package com.zj.domain.repository.demand;

import com.zj.contract.rpc.PageSize;
import com.zj.domain.entity.bo.demand.DemandBO;
import com.zj.domain.entity.bo.demand.DemandQueryBO;
import java.util.List;
import com.zj.domain.repository.marker.ConfigCommandRepository;


/**
 * @author guyuelan
 */
public interface IDemandCommandRepository extends IDemandQueryRepository, ConfigCommandRepository {

  /**
  * 创建需求
  * @param demand 需求信息
  * @return 是否成功
  */
  boolean createDemand(DemandBO demand);

  /**
  * 更新需求
  * @param demand 需求信息
  * @return 是否成功
  */
  boolean updateDemand(DemandBO demand);

  /**
  * 删除需求
  * @param demandId 需求ID
  * @return 是否成功
  */
  boolean deleteDemand(String demandId);

  /**
  * 批量更新需求状态
  * @param demandIds 需求ID列表
  * @param status 状态
  * @return 是否成功
  */
  boolean batchUpdateStatus(List<String> demandIds, int status);

  /**
  * 批量处理更新需求进入处理中
  * @param demandIds 需求列表
  * @return 是否成功
  */
  boolean batchUpdateProcessing(List<String> demandIds);

}
