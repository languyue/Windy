package com.zj.contract.pipeline.node;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author guyuelan
 * @since 2022/5/25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HttpContext extends RequestContext {

  private String url;

  /**
   * 请求体，按参数声明类型在组装阶段还原，客户端直接按对象序列化，不再包一层 JSON 字符串
   */
  private Map<String, Object> body;

  private String bodyType;

  private Map<String, String> headers;
}
