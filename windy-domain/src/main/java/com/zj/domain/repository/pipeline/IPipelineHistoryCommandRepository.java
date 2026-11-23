package com.zj.domain.repository.pipeline;

import com.zj.contract.enums.ProcessStatus;
import com.zj.domain.entity.bo.pipeline.PipelineHistoryBO;
import java.util.List;
import com.zj.domain.repository.marker.RuntimeCommandRepository;

/**
* @author guyuelan
* @since 2023/5/15
*/
public interface IPipelineHistoryCommandRepository extends IPipelineHistoryQueryRepository, RuntimeCommandRepository {

  /**
  * 创建流水线历史
  * @param pipelineHistoryBO 历史信息
  * @return 是否成功
  */
  boolean createPipelineHistory(PipelineHistoryBO pipelineHistoryBO);

  /**
  * 更新流水线历史状态
  * @param historyId 历史ID
  * @param processStatus 状态
  * @return 是否成功
  */
  boolean updateStatus(String historyId, ProcessStatus processStatus);

  /**
  * 删除流水线历史
  * @param historyId 历史ID
  * @return 是否成功
  */
  boolean deleteByHistoryId(String historyId);

  boolean deleteByPipelineId(String pipelineId);

}
