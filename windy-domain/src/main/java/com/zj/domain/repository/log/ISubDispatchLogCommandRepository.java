package com.zj.domain.repository.log;

import com.zj.domain.entity.bo.log.SubDispatchLogBO;
import java.util.List;
import com.zj.domain.repository.marker.RuntimeCommandRepository;

/**
* @author guyuelan
* @since 2023/5/19
*/
public interface ISubDispatchLogCommandRepository extends ISubDispatchLogQueryRepository, RuntimeCommandRepository {

  /**
  * 批量保存日志
  * @param subTaskLogs 日志列表
  */
  void batchSaveLogs(List<SubDispatchLogBO> subTaskLogs);

  /**
  * 更新日志状态
  * @param logId 日志ID
  * @param executeId 执行ID
  * @param status 状态
  */
  void updateLogStatus(String logId, String executeId, Integer status);

  /**
  * 批量删除日志
  * @param logIds 日志ID列表
  */
  boolean batchDeleteByLogIds(List<String> logIds);

  /**
  * 更新子任务日志客户端IP
  * @param logId 日志ID
  * @param nodeId 节点ID
  * @param clientIp 客户端IP
  */
  void updateSubLogClientIp(String logId, String nodeId, String clientIp);

}
