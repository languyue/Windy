package com.zj.infrastructure.repository.log;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.zj.common.utils.OrikaUtil;
import com.zj.contract.json.ContractJson;
import com.zj.domain.entity.bo.log.SubDispatchLogBO;
import com.zj.infrastructure.entity.po.log.SubDispatchLog;
import com.zj.infrastructure.mapper.log.SubTaskLogMapper;
import com.zj.domain.repository.log.ISubDispatchLogCommandRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * @author guyuelan
 * @since 2023/5/19
 */
@Repository
public class SubDispatchLogRepository extends
    ServiceImpl<SubTaskLogMapper, SubDispatchLog> implements ISubDispatchLogCommandRepository {

  @Override
  public void batchSaveLogs(List<SubDispatchLogBO> subTaskLogs) {
    List<SubDispatchLog> subLogs = subTaskLogs.stream().map(this::convertSubDispatchLog)
        .collect(Collectors.toList());
    saveBatch(subLogs);
  }

  @Override
  public void updateLogStatus(String logId, String executeId, Integer status) {
    SubDispatchLogBO existLog = getSubDispatchLog(logId, executeId);
    if (Objects.isNull(existLog)) {
      return;
    }

    SubDispatchLog subDispatchLog = new SubDispatchLog();
    subDispatchLog.setLogId(logId);
    subDispatchLog.setExecuteId(executeId);
    subDispatchLog.setStatus(status);
    update(subDispatchLog,
        Wrappers.lambdaUpdate(SubDispatchLog.class).eq(SubDispatchLog::getLogId, logId)
            .eq(SubDispatchLog::getExecuteId, executeId));
  }

  @Override
  public List<SubDispatchLogBO> getSubLogByLogId(String logId) {
    List<SubDispatchLog> taskLogs = list(
        Wrappers.lambdaQuery(SubDispatchLog.class).eq(SubDispatchLog::getLogId, logId));
    return taskLogs.stream().map(this::convertSubDispatchLogBO).collect(Collectors.toList());
  }

  @Override
  public boolean batchDeleteByLogIds(List<String> logIds) {
    return remove(Wrappers.lambdaQuery(SubDispatchLog.class).in(SubDispatchLog::getLogId, logIds));
  }

  @Override
  public SubDispatchLogBO getSubDispatchLog(String logId, String executeId) {
    SubDispatchLog subDispatchLog = getOne(
        Wrappers.lambdaQuery(SubDispatchLog.class).eq(SubDispatchLog::getLogId, logId)
            .eq(SubDispatchLog::getExecuteId, executeId));
    return convertSubDispatchLogBO(subDispatchLog);
  }

  @Override
  public void updateSubLogClientIp(String logId, String executeId, String clientIp) {
    SubDispatchLog subDispatchLog = new SubDispatchLog();
    subDispatchLog.setLogId(logId);
    subDispatchLog.setExecuteId(executeId);
    subDispatchLog.setClientIp(clientIp);

    update(subDispatchLog,
        Wrappers.lambdaQuery(SubDispatchLog.class).eq(SubDispatchLog::getLogId, logId)
            .eq(SubDispatchLog::getExecuteId, executeId));
  }

  private SubDispatchLog convertSubDispatchLog(SubDispatchLogBO subTaskLog) {
    SubDispatchLog subDispatchLog = OrikaUtil.convert(subTaskLog, SubDispatchLog.class);
    Map<String, Object> executeParams = subTaskLog.getExecuteParams();
    subDispatchLog.setExecuteParam(
        Objects.isNull(executeParams) ? null : ContractJson.write(executeParams));
    return subDispatchLog;
  }

  private SubDispatchLogBO convertSubDispatchLogBO(SubDispatchLog subDispatchLog) {
    if (Objects.isNull(subDispatchLog)) {
      return null;
    }
    SubDispatchLogBO subTaskLog = OrikaUtil.convert(subDispatchLog, SubDispatchLogBO.class);
    String executeParam = subDispatchLog.getExecuteParam();
    if (Objects.nonNull(executeParam) && !executeParam.trim().isEmpty()) {
      subTaskLog.setExecuteParams(
          ContractJson.read(executeParam, new TypeReference<Map<String, Object>>() {
          }));
    }
    return subTaskLog;
  }
}
