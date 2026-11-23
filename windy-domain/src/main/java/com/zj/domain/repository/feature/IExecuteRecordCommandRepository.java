package com.zj.domain.repository.feature;

import com.zj.domain.entity.bo.feature.ExecuteRecordBO;
import java.util.List;
import com.zj.domain.repository.marker.RuntimeCommandRepository;

/**
* @author guyuelan
* @since 2023/5/17
*/
public interface IExecuteRecordCommandRepository extends IExecuteRecordQueryRepository, RuntimeCommandRepository {

  /**
  * 保存执行记录
  * @param executeRecord 执行记录
  * @return 是否成功
  */
  boolean saveRecord(ExecuteRecordBO executeRecord);

  /**
  * 更新执行记录的状态和结果
  * @param executeRecord 执行记录
  * @return 是否成功
  */
  boolean updateStatusAndResult(ExecuteRecordBO executeRecord);

  /**
  * 删除执行记录
  * @param historyId 历史记录ID
  * @return 是否成功
  */
  boolean deleteByHistoryId(String historyId);

  /**
  * 批量删除执行记录
  * @param historyIds 历史记录ID列表
  * @return 是否成功
  */
  boolean batchDeleteByHistoryId(List<String> historyIds);

}
