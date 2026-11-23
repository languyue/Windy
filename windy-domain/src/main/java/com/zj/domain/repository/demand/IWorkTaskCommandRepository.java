package com.zj.domain.repository.demand;

import com.zj.contract.rpc.PageSize;
import com.zj.domain.entity.bo.demand.TaskQueryBO;
import com.zj.domain.entity.bo.demand.WorkTaskBO;
import java.util.List;
import com.zj.domain.repository.marker.ConfigCommandRepository;


/**
 * @author guyuelan
 */
public interface IWorkTaskCommandRepository extends IWorkTaskQueryRepository, ConfigCommandRepository {

  /**
  * 创建任务
  * @param workTask 任务信息
  * @return 是否成功
  */
  boolean createTask(WorkTaskBO workTask);

  /**
  * 更新任务
  * @param workTask 任务信息
  * @return 是否成功
  */
  boolean updateWorkTask(WorkTaskBO workTask);

  /**
  * 删除任务
  * @param taskId 任务ID
  * @return 是否成功
  */
  boolean deleteWorkTask(String taskId);

  /**
  * 批量更新任务状态
  * @param notCompleteTaskIds 未完成任务ID列表
  * @param status 状态
  * @return 是否成功
  */
  boolean batchUpdateStatus(List<String> notCompleteTaskIds, int status);

}
