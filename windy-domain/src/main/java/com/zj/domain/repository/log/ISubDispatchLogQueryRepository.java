package com.zj.domain.repository.log;

import com.zj.domain.entity.bo.log.SubDispatchLogBO;
import java.util.List;

/**
* @author guyuelan
* @since 2023/5/19
*/
public interface ISubDispatchLogQueryRepository {

  /**
  * 获取日志
  * @param logId 日志ID
  * @return 日志信息
  */
  List<SubDispatchLogBO> getSubLogByLogId(String logId);

  /**
  * 获取子任务记录日志
  * @param logId 日志ID
  * @param executeId 执行ID
  * @return 日志信息
  */
  SubDispatchLogBO getSubDispatchLog(String logId, String executeId);

}
