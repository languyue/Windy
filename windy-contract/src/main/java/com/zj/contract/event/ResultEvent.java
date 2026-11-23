package com.zj.contract.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.zj.contract.enums.NotifyType;
import com.zj.contract.enums.ProcessStatus;
import com.zj.contract.event.payload.ExecuteRecordPayload;
import com.zj.contract.event.payload.FeatureHistoryPayload;
import com.zj.contract.event.payload.GenerateRecordPayload;
import com.zj.contract.event.payload.NodeRecordPayload;
import com.zj.contract.event.payload.NotifyPayload;
import lombok.Data;

import java.util.Map;

/**
 * @author guyuelan
 * @since 2023/5/16
 */
@Data
public class ResultEvent {
  /**
   * 执行任务的日志ID
   * */
  private String logId;

  /**
   * 分配任务的主节点IP
   */
  private String masterIP;

  /**
   * 执行类型
   */
  private String executeType;

  /**
   * 实际实行的client节点IP
   */
  private String clientIp;

  /**
   * 任务ID（流水线Id、用例Id、任务Id）
   */
  private String executeId;

  /**
   * 结果通知的类型
   */
  private NotifyType notifyType;

  /**
   * 任务状态
   */
  private ProcessStatus status;

  /**
   * 执行完成之后需要存储的临时上下文。这个上下文主要是为了任务的后续子任务使用，用来存储运行过程中的临时全局变量
   */
  private Map<String, Object> context;

  /**
   * 用来存放任务详细信息，具体类型由 {@link #notifyType} 决定
   */
  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY,
      property = "notifyType")
  @JsonSubTypes({
      @JsonSubTypes.Type(value = NodeRecordPayload.class, name = "CREATE_NODE_RECORD"),
      @JsonSubTypes.Type(value = NodeRecordPayload.class, name = "UPDATE_NODE_RECORD"),
      @JsonSubTypes.Type(value = ExecuteRecordPayload.class, name = "CREATE_EXECUTE_POINT_RECORD"),
      @JsonSubTypes.Type(value = ExecuteRecordPayload.class, name = "UPDATE_EXECUTE_POINT_RECORD"),
      @JsonSubTypes.Type(value = FeatureHistoryPayload.class, name = "CREATE_FEATURE_HISTORY"),
      @JsonSubTypes.Type(value = FeatureHistoryPayload.class, name = "UPDATE_FEATURE_HISTORY"),
      @JsonSubTypes.Type(value = GenerateRecordPayload.class, name = "CREATE_GENERATE_MAVEN"),
      @JsonSubTypes.Type(value = GenerateRecordPayload.class, name = "UPDATE_GENERATE_MAVEN")})
  private NotifyPayload params;

  public ResultEvent masterIP(String masterIP) {
    this.masterIP = masterIP;
    return this;
  }

  public ResultEvent executeId(String executeId) {
    this.executeId = executeId;
    return this;
  }

  public ResultEvent notifyType(NotifyType notifyType) {
    this.notifyType = notifyType;
    return this;
  }

  public ResultEvent executeType(String executeType) {
    this.executeType = executeType;
    return this;
  }

  public ResultEvent status(ProcessStatus status) {
    this.status = status;
    return this;
  }

  public ResultEvent params(NotifyPayload params) {
    this.params = params;
    return this;
  }

  public ResultEvent logId(String logId) {
    this.logId = logId;
    return this;
  }

  public ResultEvent clientIp(String clientIp) {
    this.clientIp = clientIp;
    return this;
  }

  public ResultEvent context(Map<String, Object> context) {
    this.context = context;
    return this;
  }
}
