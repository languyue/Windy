package com.zj.domain.repository.feature;

import com.zj.contract.rpc.PageSize;
import com.zj.domain.entity.bo.feature.TaskRecordBO;
import java.util.List;
import com.zj.domain.repository.marker.RuntimeCommandRepository;

/**
* @author guyuelan
* @since 2023/5/17
*/
public interface ITaskRecordCommandRepository extends ITaskRecordQueryRepository, RuntimeCommandRepository {

  /**
  * 保存任务记录
  * @param taskRecordBO 任务记录
  * @return 是否成功
  */
  boolean save(TaskRecordBO taskRecordBO);

  /**
  * 更新任务记录状态
  *
  * @param recordId 记录ID
  * @param status   状态
  * @return 是否成功
  */
  boolean updateRecordStatus(String recordId, int status);

  /**
  * 更新任务记录状态和百分比
  *
  * @param recordId 记录ID
  * @param status   状态
  * @param percent  百分比
  * @return 是否成功
  */
  boolean updateRecordStatusAndPercent(String recordId, Integer status, int percent);

  /**
  * 删除任务记录
  * @param recordId 记录ID
  * @return 是否成功
  */
  boolean deleteTaskRecord(String recordId);

}
