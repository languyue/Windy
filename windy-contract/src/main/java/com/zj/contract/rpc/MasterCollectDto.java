package com.zj.contract.rpc;

import com.zj.contract.monitor.PhysicsCollect;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/7/4
 */
@Data
public class MasterCollectDto {

  /**
   * Client当前版本
   */
  private String version;

  /**
   * 采集的物理监控信息
   */
  private PhysicsCollect physics;

  /**
   * 分配任务正在处理的个数
   */
  private Integer taskCount;
}
