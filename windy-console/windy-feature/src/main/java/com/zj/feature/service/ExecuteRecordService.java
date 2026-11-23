package com.zj.feature.service;

import com.zj.domain.entity.bo.feature.ExecuteRecordBO;
import com.zj.domain.repository.feature.IExecuteRecordQueryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * execute_record 的写权在 master，console 只读用于展示执行明细。
 */
@Service
public class ExecuteRecordService {

  private final IExecuteRecordQueryRepository executeRecordRepository;

  public ExecuteRecordService(IExecuteRecordQueryRepository executeRecordRepository) {
    this.executeRecordRepository = executeRecordRepository;
  }

  public List<ExecuteRecordBO> getExecuteRecords(String historyId) {
    return executeRecordRepository.getExecuteRecords(historyId);
  }
}
