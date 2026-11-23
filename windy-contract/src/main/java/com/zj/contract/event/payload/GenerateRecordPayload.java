package com.zj.contract.event.payload;

import com.zj.contract.generate.GenerateDetail;
import java.util.List;
import lombok.Data;

/**
 * 二方包生成记录的通知载荷。
 *
 * @author guyuelan
 */
@Data
public class GenerateRecordPayload implements NotifyPayload {

  private String recordId;

  /**
   * 服务Id
   * */
  private String serviceId;

  /**
   * 执行参数
   * */
  private GenerateDetail generateParams;

  /**
   * 执行结果记录
   * */
  private List<String> generateResult;

  /**
   * 构建的版本号
   */
  private String version;

  /**
   * 执行状态
   * */
  private Integer status;

  private Long createTime;

  private Long updateTime;
}
