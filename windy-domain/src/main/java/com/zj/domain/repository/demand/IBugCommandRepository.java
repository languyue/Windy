package com.zj.domain.repository.demand;

import com.zj.contract.rpc.PageSize;
import com.zj.domain.entity.bo.demand.BugBO;
import com.zj.domain.entity.bo.demand.BugQueryBO;
import java.util.List;
import com.zj.domain.repository.marker.ConfigCommandRepository;


/**
 * @author guyuelan
 */
public interface IBugCommandRepository extends IBugQueryRepository, ConfigCommandRepository {

  /**
  * 创建bug
  * @param bugBO bug信息
  * @return 是否成功
  */
  boolean createBug(BugBO bugBO);

  /**
  * 更新bug
  * @param bugBO bug信息
  * @return 是否成功
  */
  boolean updateBug(BugBO bugBO);

  /**
  * 删除bug
  * @param bugId 缺陷ID
  * @return 是否成功
  */
  boolean deleteBug(String bugId);

  /**
  * 批量更新缺陷状态
  * @param bugIds 缺陷ID列表
  * @param status 状态
  * @return 是否成功
  */
  boolean batchUpdateStatus(List<String> bugIds, int status);

  /**
  * 批量缺陷更新处理中
  * @param notCompleteBugIds 缺陷ID列表
  * @return 是否成功
  */
  boolean batchUpdateProcessing(List<String> notCompleteBugIds);

}
