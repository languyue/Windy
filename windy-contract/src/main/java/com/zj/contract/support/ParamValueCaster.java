package com.zj.contract.support;

import com.zj.contract.json.ContractJson;
import com.zj.plugin.loader.ParamValueType;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 按声明类型把配置里的参数文本还原成具体值。
 *
 * <p>页面与数据库里参数值都是字符串，进入运行态之前在这里一次还原，之后的调用链只处理对象。
 * 解析失败时保留原始文本：一个配置错的参数不应该让整条任务直接抛异常。</p>
 *
 * @author guyuelan
 */
public final class ParamValueCaster {

  private ParamValueCaster() {
  }

  /**
   * 已经是对象的值原样返回，避免把嵌套结构重新压成字符串再解析
   */
  public static Object cast(Object value, String type) {
    if (Objects.isNull(value) || Objects.isNull(type)) {
      return value;
    }
    if (!(value instanceof String)) {
      return value;
    }
    return cast((String) value, type);
  }

  public static Object cast(String value, String type) {
    if (Objects.isNull(value) || Objects.isNull(type)) {
      return value;
    }
    if (ParamValueType.String.name().equals(type) || ParamValueType.Object.name().equals(type)) {
      return value;
    }
    if (ParamValueType.Boolean.name().equals(type)) {
      return castValue(value, Boolean::valueOf);
    }
    if (ParamValueType.Integer.name().equals(type)) {
      return castValue(value, Integer::valueOf);
    }
    if (ParamValueType.Long.name().equals(type)) {
      return castValue(value, Long::valueOf);
    }
    if (ParamValueType.Float.name().equals(type)) {
      return castValue(value, Float::valueOf);
    }
    if (ParamValueType.Double.name().equals(type)) {
      return castValue(value, Double::valueOf);
    }
    if (ParamValueType.Map.name().equals(type)) {
      return castJson(value, () -> ContractJson.readMap(value));
    }
    if (ParamValueType.Array.name().equals(type)) {
      return castJson(value, () -> ContractJson.readList(value, Object.class));
    }
    return value;
  }

  private static Object castValue(String value, Function<String, Object> parser) {
    if (value.trim().isEmpty()) {
      return value;
    }
    try {
      return parser.apply(value.trim());
    } catch (RuntimeException e) {
      return value;
    }
  }

  private static Object castJson(String value, Supplier<Object> parser) {
    if (value.trim().isEmpty()) {
      return value;
    }
    try {
      Object result = parser.get();
      return Objects.isNull(result) ? value : result;
    } catch (RuntimeException e) {
      return value;
    }
  }
}
