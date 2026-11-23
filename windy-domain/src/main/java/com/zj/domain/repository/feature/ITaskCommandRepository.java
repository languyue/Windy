package com.zj.domain.repository.feature;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.domain.entity.bo.feature.TaskInfoBO;
import com.zj.domain.entity.po.feature.TaskInfo;
import java.util.List;
import com.zj.domain.repository.marker.ConfigCommandRepository;

/**
* @author guyuelan
* @since 2023/5/17
*/
public interface ITaskCommandRepository extends ITaskQueryRepository, ConfigCommandRepository {

  /**
  * 创建任务
  * @param taskInfoBO 任务信息
  * @return 是否成功
  */
  boolean createTask(TaskInfoBO taskInfoBO);

  /**
  * 更新任务
  * @param taskInfoBO 任务信息
  * @return 是否成功
  */
  boolean updateTask(TaskInfoBO taskInfoBO);

  /**
  * 删除任务
  * @param taskId 任务ID
  * @return 是否成功
  */
  boolean deleteTask(String taskId);

}
