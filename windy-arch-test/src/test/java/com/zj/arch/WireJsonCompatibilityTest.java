package com.zj.arch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zj.contract.json.WireJson;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 出站报文语义守卫。
 *
 * <p>RestTemplate 原来用 fastjson，并开了 WriteMapNullValue / WriteNullStringAsEmpty /
 * WriteNullNumberAsZero / WriteNullListAsEmpty。换成 {@link WireJson}（Jackson）后这些空值语义
 * 必须原样保留：接收方一直在按「字段一定存在」的报文本写，null 变成缺失会引发出 NPE 或状态判断偏差。</p>
 *
 * @author guyuelan
 */
public class WireJsonCompatibilityTest {

  private final ObjectMapper mapper = WireJson.mapper();

  @Test
  void nullFieldsShouldBeWrittenAsFastjsonDid() throws Exception {
    String json = mapper.writeValueAsString(new WireSample());
    assertTrue(json.contains("\"name\":\"\""), "null 字符串要写成空串: " + json);
    assertTrue(json.contains("\"count\":0"), "null 数字要写成 0: " + json);
    assertTrue(json.contains("\"flag\":false"), "null 布尔要写成 false: " + json);
    assertTrue(json.contains("\"items\":[]"), "null 集合要写成空数组: " + json);
    assertTrue(json.contains("\"extra\":null"), "无类型的 null 仍然写 null: " + json);
  }

  @Test
  void unknownPropertyShouldBeIgnored() throws Exception {
    WireSample sample = mapper.readValue(
        "{\"name\":\"a\",\"removed\":1}", WireSample.class);
    assertEquals("a", sample.getName());
    assertNull(sample.getFlag());
  }

  @Test
  void numberShouldBeCoercedToString() throws Exception {
    WireSample sample = mapper.readValue("{\"name\":123}", WireSample.class);
    assertEquals("123", sample.getName());
  }

  private static class WireSample {

    private String name;

    private Integer count;

    private Boolean flag;

    private List<String> items;

    private Object extra;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Integer getCount() {
      return count;
    }

    public void setCount(Integer count) {
      this.count = count;
    }

    public Boolean getFlag() {
      return flag;
    }

    public void setFlag(Boolean flag) {
      this.flag = flag;
    }

    public List<String> getItems() {
      return items;
    }

    public void setItems(List<String> items) {
      this.items = items;
    }

    public Object getExtra() {
      return extra;
    }

    public void setExtra(Object extra) {
      this.extra = extra;
    }
  }
}
