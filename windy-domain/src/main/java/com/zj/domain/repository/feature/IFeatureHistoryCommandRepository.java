package com.zj.domain.repository.feature;

import com.zj.contract.enums.ProcessStatus;
import com.zj.domain.entity.bo.feature.FeatureHistoryBO;
import java.util.List;
import com.zj.domain.repository.marker.RuntimeCommandRepository;

/**
* @author guyuelan
* @since 2023/5/17
*/
public interface IFeatureHistoryCommandRepository extends IFeatureHistoryQueryRepository, RuntimeCommandRepository {

  /**
  * 保存用例历史记录
  * @param featureHistory 用例历史记录
  * @return 是否成功
  */
  boolean saveHistory(FeatureHistoryBO featureHistory);

  /**
  * 删除用例历史记录
  * @param historyId 历史ID
  * @return 是否成功
  */
  boolean deleteHistory(String historyId);

  /**
  * 删除任务的用例历史记录
  * @param taskRecordId 任务ID
  * @return 是否成功
  */
  boolean deleteByRecordId(String taskRecordId);

  /**
  * 更新用例历史记录状态
  * @param historyId 历史ID
  * @param status 状态
  * @return 是否成功
  */
  boolean updateStatus(String historyId, int status);

  /**
  * 更新用例历史记录状态
  * @param taskRecordId 任务记录ID
  * @param processStatus 状态
  * @return 是否成功
  */
  boolean stopTaskFeatures(String taskRecordId, ProcessStatus processStatus);

}
