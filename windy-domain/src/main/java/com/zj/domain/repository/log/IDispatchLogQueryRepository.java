package com.zj.domain.repository.log;

import com.zj.domain.entity.bo.log.DispatchLogBO;
import java.util.List;

/**
* @author guyuelan
* @since 2023/5/19
*/
public interface IDispatchLogQueryRepository {

  /**
  * 获取运行中的事务任务
  * @return 任务列表
  */
  List<DispatchLogBO> getRunningDispatchLog();

  /**
  * 获取日志
  * @param logId 日志ID
  * @return 日志信息
  */
  DispatchLogBO getDispatchLog(String logId);

}
