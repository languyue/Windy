package com.zj.domain.repository.log;

import com.zj.domain.entity.dto.log.DispatchLogDto;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/19
 */
public interface IDispatchLogRepository {

  List<DispatchLogDto> getRunningTaskLog();

  List<String> delete7DayLog();

  void saveLog(DispatchLogDto taskLog);

  void updateLogStatus(String logId, int type);

  boolean updateLogMasterIp(String logId, String localIP, Integer lockVersion);

  void updateLogSourceRecord(String logId, String recordId);

  DispatchLogDto getDispatchLogBySourceId(String targetId);
}
