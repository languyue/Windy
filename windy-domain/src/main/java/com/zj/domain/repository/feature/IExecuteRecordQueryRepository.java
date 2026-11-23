package com.zj.domain.repository.feature;

import com.zj.domain.entity.bo.feature.ExecuteRecordBO;
import java.util.List;

/**
* @author guyuelan
* @since 2023/5/17
*/
public interface IExecuteRecordQueryRepository {

  /**
  * 获取执行记录
  * @param historyId 历史记录ID
  * @return 执行记录
  */
  List<ExecuteRecordBO> getExecuteRecords(String historyId);

}
