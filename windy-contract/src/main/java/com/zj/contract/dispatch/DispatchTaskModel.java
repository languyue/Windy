package com.zj.contract.dispatch;

import com.zj.contract.enums.LogType;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
@Data
public class DispatchTaskModel {

  /**
   * 执行任务来源Id列表(测试集合Id，流水线Id，用例任务Id)
   * 批量执行测试集时这里是批量勾选的用例Id
   * */
  private List<String> sourceIds;

  /**
   * 执行任务来源名称
   * */
  private String sourceName;

  /**
   * 任务类型{@link LogType}
   * */
  private Integer type;

  /**
   * 触发源头方，如用例任务记录Id、apiId列表
   */
  private List<String> triggerIds;

  /**
   * 任务触发人
   */
  private String user;

  public DispatchTaskModel() {
  }

  /**
   * 单来源任务的便捷入口，绝大多数调度只有一个来源Id
   */
  public DispatchTaskModel(String sourceId, String sourceName, Integer type) {
    this.sourceIds = Collections.singletonList(sourceId);
    this.sourceName = sourceName;
    this.type = type;
  }

  /**
   * 调度执行时按单一来源定位，方法名不带 get 前缀，避免被当作 JSON 属性写出
   */
  public String firstSourceId() {
    return firstOf(sourceIds);
  }

  public String firstTriggerId() {
    return firstOf(triggerIds);
  }

  private static String firstOf(List<String> values) {
    if (Objects.isNull(values) || values.isEmpty()) {
      return null;
    }
    return values.get(0);
  }
}
