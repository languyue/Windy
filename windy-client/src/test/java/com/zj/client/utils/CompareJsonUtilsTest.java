package com.zj.client.utils;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 断言比较的口径回归：数字与同文本的字符串不算相等，避免 1 与 "1" 被误判为通过。
 *
 * <p>比较引擎面对的是任意第三方响应，仍然是 fastjson 的动态结构，这里只锁住结果口径。</p>
 *
 * @author guyuelan
 */
public class CompareJsonUtilsTest {

  @Test
  void numberAndSameTextShouldNotBeEqual() {
    Map<String, Object> difference = CompareJsonUtils.compareJsonObject("{\"v\":1}", "{\"v\":\"1\"}");
    assertFalse(difference.isEmpty(), "1 与 \"1\" 必须判为不同，实际=" + difference);
  }

  @Test
  void sameTextShouldBeEqual() {
    Map<String, Object> difference = CompareJsonUtils.compareJsonObject("{\"v\":\"1\"}", "{\"v\":\"1\"}");
    assertTrue(difference.isEmpty(), "文本相同应无差异，实际=" + difference);
  }

  @Test
  void nestedFieldShouldBeCompared() {
    Map<String, Object> difference = CompareJsonUtils.compareJsonObject(
        "{\"a\":{\"b\":[1,2]}}", "{\"a\":{\"b\":[1,3]}}");
    assertFalse(difference.isEmpty(), "嵌套数组差异必须识别，实际=" + difference);
  }
}
