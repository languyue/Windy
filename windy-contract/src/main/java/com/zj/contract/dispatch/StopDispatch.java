package com.zj.contract.dispatch;

import com.zj.contract.enums.LogType;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
@Data
public class StopDispatch {
  /**
   * 用例、任务、流水线记录Id
   * */
  private String targetId;

  /**
   * 任务类型
   * */
  private LogType logType;
}
