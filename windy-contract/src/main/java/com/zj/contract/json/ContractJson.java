package com.zj.contract.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.util.List;
import java.util.Map;

/**
 * 全仓唯一的「对象 ↔ JSON 字符串」出入口。
 *
 * <p>只有两个地方允许调用它：{@code windy-domain/repository} 读写 PO 的 JSON 列，
 * 以及 client 落盘 {@code persist.log}。HTTP 边界由 RestTemplate 的 Jackson 消息转换器
 * 一次性完成序列化与反序列化，业务代码里不再出现手工的字符串转对象。</p>
 *
 * @author guyuelan
 */
public final class ContractJson {

  private static final ObjectMapper MAPPER = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
      .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
      .registerModule(new Jdk8Module());

  private ContractJson() {
  }

  public static ObjectMapper mapper() {
    return MAPPER;
  }

  public static <T> T read(String json, Class<T> type) {
    if (isEmpty(json)) {
      return null;
    }
    try {
      return MAPPER.readValue(json, type);
    } catch (JsonProcessingException e) {
      throw new ContractJsonException("parse json to " + type.getName() + " error", e);
    }
  }

  /**
   * 内存对象之间按 JSON 语义转换（Bean ↔ Map），不经过字符串。
   * 用于把无具体类型的数据对齐成统一结构再取字段。
   */
  public static <T> T convert(Object source, Class<T> type) {
    return MAPPER.convertValue(source, type);
  }

  public static <T> T convert(Object source, TypeReference<T> type) {
    return MAPPER.convertValue(source, type);
  }

  public static <T> List<T> readList(String json, Class<T> type) {
    if (isEmpty(json)) {
      return null;
    }
    CollectionType collectionType = MAPPER.getTypeFactory().constructCollectionType(List.class, type);
    try {
      return MAPPER.readValue(json, collectionType);
    } catch (JsonProcessingException e) {
      throw new ContractJsonException("parse json to list of " + type.getName() + " error", e);
    }
  }

  /**
   * 按类型名解析，处理一个列里承载多种结构的泛型场景
   */
  @SuppressWarnings("unchecked")
  public static <T> T read(String json, String typeName) {
    if (isEmpty(json) || isEmpty(typeName)) {
      return null;
    }
    try {
      return (T) read(json, Class.forName(typeName));
    } catch (ClassNotFoundException e) {
      throw new ContractJsonException("can not find class " + typeName, e);
    }
  }

  /**
   * 泛型场景（如 Map&lt;String, String&gt;）用 TypeReference 保类型
   */
  public static <T> T read(String json, TypeReference<T> type) {
    if (isEmpty(json)) {
      return null;
    }
    try {
      return MAPPER.readValue(json, type);
    } catch (JsonProcessingException e) {
      throw new ContractJsonException("parse json to " + type.getType() + " error", e);
    }
  }

  public static Map<String, Object> readMap(String json) {
    if (isEmpty(json)) {
      return null;
    }
    JavaType mapType = MAPPER.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
    try {
      return MAPPER.readValue(json, mapType);
    } catch (JsonProcessingException e) {
      throw new ContractJsonException("parse json to map error", e);
    }
  }

  /**
   * 读成树：第三方报文（git 平台、yapi、postman）结构不固定，先按树取关键字段再组业务对象
   */
  public static JsonNode readTree(String json) {
    if (isEmpty(json)) {
      return null;
    }
    try {
      return MAPPER.readTree(json);
    } catch (JsonProcessingException e) {
      throw new ContractJsonException("parse json to tree error", e);
    }
  }

  public static String write(Object obj) {
    if (obj == null) {
      return null;
    }
    try {
      return MAPPER.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new ContractJsonException("write object to json error", e);
    }
  }

  private static boolean isEmpty(String value) {
    return value == null || value.trim().isEmpty();
  }
}
