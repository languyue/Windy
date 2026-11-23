package com.zj.contract.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * 进程之间 HTTP 传输专用的 Jackson 配置。
 *
 * <p>历史链路上 RestTemplate 用的是 fastjson，并打开了
 * {@code WriteMapNullValue / WriteNullStringAsEmpty / WriteNullNumberAsZero / WriteNullListAsEmpty}。
 * 也就是说「null 会被写成 ""、0、[]」已经是既有的线上语义，接收方（Spring MVC 默认 Jackson）
 * 早就按这套数据在跑。直接把出站序列化换成裸 Jackson 会让这些字段从「有值」变成「缺失」，
 * 例如接收方拿到 null 集合再遍历就会 NPE。因此这里把这四个语义显式复刻一遍，
 * 保证换序列化器前后报文等价。</p>
 *
 * <p>与 {@link ContractJson} 的分工：ContractJson 负责「对象 ↔ 数据库 JSON 列」，保持与
 * fastjson {@code toJSONString} 一致（省略 null）；本类负责「对象 ↔ HTTP 报文」，保持与
 * RestTemplate 既有 fastjson 配置一致（null 写成空值）。两者都不出现在业务代码里。</p>
 *
 * @author guyuelan
 */
public final class WireJson {

  private static final ObjectMapper MAPPER = createMapper();

  private WireJson() {
  }

  public static ObjectMapper mapper() {
    return MAPPER;
  }

  /**
   * 直接写出 HTTP 报文，空值按既有 fastjson 语义补齐
   */
  public static String write(Object obj) {
    try {
      return MAPPER.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new ContractJsonException("write object to wire json error", e);
    }
  }

  private static ObjectMapper createMapper() {
    SimpleModule nullValueModule = new SimpleModule();
    nullValueModule.setSerializerModifier(new EmptyValueOnNullModifier());
    return new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        // fastjson 遇到空对象直接写出 {}，这里对齐，避免无 getter 的报文让序列化直接失败
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        .setSerializationInclusion(JsonInclude.Include.ALWAYS)
        .registerModule(new Jdk8Module())
        .registerModule(nullValueModule);
  }

  /**
   * 按属性声明类型给 null 值挂上空值序列化器，等价于 fastjson 的四个 WriteNullXxx 特性
   */
  private static class EmptyValueOnNullModifier extends BeanSerializerModifier {

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
        BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
      for (BeanPropertyWriter writer : beanProperties) {
        Class<?> rawType = writer.getType().getRawClass();
        if (rawType == String.class) {
          writer.assignNullSerializer(new EmptyStringSerializer());
        } else if (Number.class.isAssignableFrom(rawType)) {
          writer.assignNullSerializer(new ZeroNumberSerializer());
        } else if (rawType == Boolean.class) {
          writer.assignNullSerializer(new FalseBooleanSerializer());
        } else if (Collection.class.isAssignableFrom(rawType) || rawType.isArray()) {
          writer.assignNullSerializer(new EmptyArraySerializer());
        }
      }
      return beanProperties;
    }
  }

  private static class EmptyStringSerializer extends JsonSerializer<Object> {
    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeString("");
    }
  }

  private static class ZeroNumberSerializer extends JsonSerializer<Object> {
    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeNumber(0);
    }
  }

  private static class FalseBooleanSerializer extends JsonSerializer<Object> {
    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeBoolean(false);
    }
  }

  private static class EmptyArraySerializer extends JsonSerializer<Object> {
    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeStartArray();
      gen.writeEndArray();
    }
  }
}
