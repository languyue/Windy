package com.zj.infrastructure.repository.log;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.zj.contract.enums.ProcessStatus;
import com.zj.contract.json.ContractJson;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.log.DispatchLogBO;
import com.zj.infrastructure.entity.po.log.DispatchLog;
import com.zj.infrastructure.mapper.log.DispatchLogMapper;
import com.zj.domain.repository.log.IDispatchLogCommandRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;

/**
 * @author guyuelan
 * @since 2023/5/19
 */
@Repository
public class DispatchLogRepository extends ServiceImpl<DispatchLogMapper, DispatchLog> implements
    IDispatchLogCommandRepository {

  public static final int PLUS_VERSION = 1;

  @Override
  public List<DispatchLogBO> getRunningDispatchLog() {
    List<DispatchLog> dispatchLogs = list(Wrappers.lambdaQuery(DispatchLog.class)
        .eq(DispatchLog::getLogStatus, ProcessStatus.RUNNING.getType()));
    return dispatchLogs.stream().map(this::convertDispatchLogBO).collect(Collectors.toList());
  }

  @Override
  public List<String> delete7DayLog() {
    long dateTime = new DateTime().minusDays(7).toDate().getTime();
    List<DispatchLog> dispatchLogs = list(
        Wrappers.lambdaQuery(DispatchLog.class).lt(DispatchLog::getCreateTime, dateTime));
    if (CollectionUtils.isEmpty(dispatchLogs)) {
      return Collections.emptyList();
    }

    boolean result = remove(
        Wrappers.lambdaQuery(DispatchLog.class).lt(DispatchLog::getCreateTime, dateTime));
    return result ? dispatchLogs.stream().map(DispatchLog::getLogId).collect(Collectors.toList())
        : Collections.emptyList();
  }

  @Override
  public void saveLog(DispatchLogBO dispatchLogBO) {
    DispatchLog dispatchLog = convertDispatchLog(dispatchLogBO);
    Long dateNow = System.currentTimeMillis();
    dispatchLog.setCreateTime(dateNow);
    dispatchLog.setUpdateTime(dateNow);
    save(dispatchLog);
  }

  @Override
  public boolean updateLogStatus(String logId, int status) {
    DispatchLog dispatchLog = new DispatchLog();
    dispatchLog.setLogId(logId);
    dispatchLog.setLogStatus(status);
    dispatchLog.setUpdateTime(System.currentTimeMillis());
    return update(dispatchLog, Wrappers.lambdaUpdate(DispatchLog.class).eq(DispatchLog::getLogId, logId));
  }

  @Override
  public boolean updateLogMasterIp(String logId, String localIP, Integer lockVersion) {
    DispatchLog dispatchLog = new DispatchLog();
    dispatchLog.setLogId(logId);
    dispatchLog.setNodeIp(localIP);
    dispatchLog.setUpdateTime(System.currentTimeMillis());
    dispatchLog.setLockVersion(lockVersion + PLUS_VERSION);
    return update(dispatchLog,
        Wrappers.lambdaUpdate(DispatchLog.class).eq(DispatchLog::getLogId, logId)
            .eq(DispatchLog::getLockVersion, lockVersion));
  }

  @Override
  public boolean updateLogSourceRecord(String logId, String sourceRecordId) {
    DispatchLog dispatchLog = new DispatchLog();
    dispatchLog.setLogId(logId);
    dispatchLog.setSourceRecordId(sourceRecordId);
    dispatchLog.setUpdateTime(System.currentTimeMillis());
    return update(dispatchLog, Wrappers.lambdaUpdate(DispatchLog.class).eq(DispatchLog::getLogId, logId));
  }

  @Override
  public DispatchLogBO getDispatchLog(String logId) {
    DispatchLog dispatchLog = getOne(
        Wrappers.lambdaQuery(DispatchLog.class).eq(DispatchLog::getLogId, logId));
    return convertDispatchLogBO(dispatchLog);
  }

  private DispatchLogBO convertDispatchLogBO(DispatchLog dispatchLog) {
    if (Objects.isNull(dispatchLog)) {
      return null;
    }
    DispatchLogBO dispatchLogBO = OrikaUtil.convert(dispatchLog, DispatchLogBO.class);
    //source_id(JSON 数组)与 sourceIds(List)字段名不同，Orika 不映射，在此显式还原
    dispatchLogBO.setSourceIds(readSourceIds(dispatchLog.getSourceId()));
    return dispatchLogBO;
  }

  private DispatchLog convertDispatchLog(DispatchLogBO dispatchLogBO) {
    DispatchLog dispatchLog = OrikaUtil.convert(dispatchLogBO, DispatchLog.class);
    //来源Id列表统一以 JSON 数组落库，运行期只传递对象
    dispatchLog.setSourceId(ContractJson.write(dispatchLogBO.getSourceIds()));
    return dispatchLog;
  }

  /**
   * 历史行可能是单个裸 Id，取不到数组时按单元素处理，
   * 避免一条旧数据让崩溃恢复扫描整体失败
   */
  private static List<String> readSourceIds(String sourceId) {
    if (StringUtils.isBlank(sourceId)) {
      return Collections.emptyList();
    }
    JsonNode node = ContractJson.readTree(sourceId);
    if (Objects.isNull(node) || !node.isArray()) {
      return Collections.singletonList(sourceId);
    }
    List<String> sourceIds = new ArrayList<>();
    node.forEach(element -> sourceIds.add(element.asText()));
    return sourceIds;
  }
}
