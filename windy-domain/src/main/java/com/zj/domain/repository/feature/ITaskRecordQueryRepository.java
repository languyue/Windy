package com.zj.domain.repository.feature;

import com.zj.contract.rpc.PageSize;
import com.zj.domain.entity.bo.feature.TaskRecordBO;
import java.util.List;

/**
* @author guyuelan
* @since 2023/5/17
*/
public interface ITaskRecordQueryRepository {

  /**
  * 分页获取任务记录列表
  *
  * @param pageNum 页码
  * @param size    每页数量
  * @param userId  用户ID
  * @return 任务记录列表
  */
  PageSize<TaskRecordBO> getTaskRecordPage(Integer pageNum, Integer size, String userId);

  /**
  * 获取任务记录
  * @param recordId 记录ID
  * @return 任务记录
  */
  TaskRecordBO getTaskRecord(String recordId);

  /**
  * 获取任务记录列表
  * @param taskId 任务ID
  * @return 任务记录列表
  */
  List<TaskRecordBO> getTaskRecords(String taskId);

  /**
  * 根据任务触发源ID获取任务记录
  * @param triggerId 触发器ID
  * @return 任务记录
  */
  TaskRecordBO getTaskRecordByTrigger(String triggerId);

  /**
  * 分页获取触发任务yuanID记录
  * @param triggerId 触发器ID
  * @param pageNum 页码
  * @param size 每页数量
  * @return 任务记录列表
  */
  PageSize<TaskRecordBO> getTriggerTaskRecords(String triggerId, Integer pageNum, Integer size);

  /**
  * 获取旧的任务记录
  * @param oldTime 时间
  * @return 任务记录列表
  */
  List<TaskRecordBO> getOldTaskRecord(long oldTime);

}
