package com.zj.domain.entity.bo.feature;

import lombok.Data;

@Data
public class TaskRecordBO {

  private Long id;

  /**
   * 任务记录ID
   * */
  private String recordId;

  /**
   * 任务ID
   * */
  private String taskId;

  /**
   * 执行状态
   * */
  private Integer status;

  /**
   * 状态名称
   */
  private String statusName;

  /**
   * 任务名称
   * */
  private String taskName;

  /**
   * 测试集Id
   * */
  private String testCaseId;

  /**
   * 任务配置
   * */
  private String taskConfig;

  /**
   * 任务执行成功率
   * */
  private Integer percent;

  /**
   * 任务记录类型: 1 用例任务记录 2 临时批量任务
   * */
  private Integer type;

  /**
   * 任务执行人
   */
  private String executeUser;

  /**
   * 任务执行人ID
   */
  private String userId;

  /**
   * 执行机器
   * */
  private String machines;

  /**
   * 触发任务的来源方，用来记录由谁触发的任务
   */
  private String triggerId;

  private Long createTime;

  private Long updateTime;
}
