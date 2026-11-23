package com.zj.contract.pipeline.node;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

/**
 * 节点执行上下文。
 *
 * <p>运行期是多态对象，报文里的 {@code type} 判别符就是子类型注册名，取值与
 * {@code com.zj.common.enums.ExecuteType#name()} 一致。判别符由序列化器按实际类型写出，
 * 不另设同名字段（避免报文里出现重复的 type 键），client 侧直接反序列化成具体子类型，
 * 不再由客户端按 executeType 猜类型。</p>
 *
 * @author guyuelan
 * @since 2022/5/24
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY,
    property = "type", defaultImpl = RequestContext.class)
@JsonSubTypes({
    @JsonSubTypes.Type(value = RequestContext.class,      name = "NONE"),
    @JsonSubTypes.Type(value = HttpContext.class,         name = "HTTP"),
    @JsonSubTypes.Type(value = DeployContext.class,       name = "DEPLOY"),
    @JsonSubTypes.Type(value = FeatureContext.class,      name = "TEST"),
    @JsonSubTypes.Type(value = ApprovalContext.class,     name = "APPROVAL"),
    @JsonSubTypes.Type(value = BuildCodeContext.class,    name = "BUILD"),
    @JsonSubTypes.Type(value = MergeMasterContext.class,  name = "MERGE"),
    @JsonSubTypes.Type(value = WaitContext.class,         name = "WAIT")})
public class RequestContext {

  private String version = "1.0";

  private String pipelineId;

  /**
   * 当前流水线所属服务的git地址
   * */
  private String gitUrl;

  /**
   * 全局git类型
   * */
  private String gitType;
  /**
   * git访问的凭据
   * gitlab场景： oauth2
   * gitea场景： 任意
   * */
  private String tokenName;

  /**
   * 访问git的token
   * */
  private String token;
}
