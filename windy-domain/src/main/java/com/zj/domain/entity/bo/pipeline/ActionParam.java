package com.zj.domain.entity.bo.pipeline;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/3/27
 */
@Data
public class ActionParam {
  private String name;
  private String description;
  private String position;
  /**
   * 参数类型，取值对应 ParamValueType 的枚举名
   */
  private String type;

  /**
   * 参数值，按 type 还原后的具体对象
   */
  private Object value;
}
