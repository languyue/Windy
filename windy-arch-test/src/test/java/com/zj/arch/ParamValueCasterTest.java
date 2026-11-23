package com.zj.arch;

import com.zj.contract.support.ParamValueCaster;
import com.zj.plugin.loader.ParamValueType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 参数文本按声明类型还原的口径回归。
 *
 * @author guyuelan
 */
public class ParamValueCasterTest {

  @Test
  void stringAndObjectShouldKeepText() {
    assertEquals("v", ParamValueCaster.cast("v", ParamValueType.String.name()));
    assertEquals("v", ParamValueCaster.cast("v", ParamValueType.Object.name()));
    assertNull(ParamValueCaster.cast((String) null, ParamValueType.String.name()));
  }

  @Test
  void numberAndBooleanShouldCast() {
    assertEquals(10, ParamValueCaster.cast("10", ParamValueType.Integer.name()));
    assertEquals(10L, ParamValueCaster.cast("10", ParamValueType.Long.name()));
    assertEquals(1.5D, ParamValueCaster.cast("1.5", ParamValueType.Double.name()));
    assertEquals(1.5F, ParamValueCaster.cast("1.5", ParamValueType.Float.name()));
    assertEquals(true, ParamValueCaster.cast("true", ParamValueType.Boolean.name()));
  }

  @Test
  void mapAndArrayShouldParseJson() {
    Object map = ParamValueCaster.cast("{\"k\":\"v\"}", ParamValueType.Map.name());
    assertTrue(map instanceof Map, "实际=" + map.getClass());
    assertEquals("v", ((Map<?, ?>) map).get("k"));

    Object array = ParamValueCaster.cast("[\"a\",\"b\"]", ParamValueType.Array.name());
    assertTrue(array instanceof List, "实际=" + array.getClass());
    assertEquals(Arrays.asList("a", "b"), array);
  }

  @Test
  void badTextShouldStayRaw() {
    assertEquals("abc", ParamValueCaster.cast("abc", ParamValueType.Integer.name()));
    assertEquals("", ParamValueCaster.cast("", ParamValueType.Integer.name()));
    assertEquals("{not json}", ParamValueCaster.cast("{not json}", ParamValueType.Map.name()));
  }

  @Test
  void objectValueShouldNotBeReparsed() {
    List<String> list = Arrays.asList("a", "b");
    assertSame(list, ParamValueCaster.cast(list, ParamValueType.Array.name()));
    assertSame(list, ParamValueCaster.cast((Object) list, ParamValueType.String.name()));
  }
}
