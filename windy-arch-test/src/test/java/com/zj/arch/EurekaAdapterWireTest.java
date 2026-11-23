package com.zj.arch;

import com.zj.common.adapter.invoker.eureka.BaseEurekaAdapter;
import com.zj.contract.json.WireJson;
import com.zj.contract.pipeline.node.HttpContext;
import com.zj.contract.pipeline.node.RequestContext;
import com.zj.contract.rpc.ResponseMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * 进程间 HTTP 通道的口径回归：报文只被转换器解析一次，调用方直接拿到具体对象。
 *
 * <p>RestTemplate 的转换器按生产配置换成 {@link WireJson}，用来验证「出站只序列化一次、
 * 入站直接是多态子类型」，即 {@code DDD 层对外是具体对象、不再有字符串二次 parse}。</p>
 *
 * @author guyuelan
 */
public class EurekaAdapterWireTest {

  private static final String DISPATCH_URL = "http://WindyMaster/v1/devops/dispatch/task";

  private static final ParameterizedTypeReference<ResponseMeta<RequestContext>> META_CONTEXT =
      new ParameterizedTypeReference<ResponseMeta<RequestContext>>() {};

  private MockRestServiceServer server;
  private TestAdapter adapter;

  @BeforeEach
  void setUp() {
    RestTemplate restTemplate = new RestTemplate();
    List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
    converters.removeIf(converter -> converter instanceof MappingJackson2HttpMessageConverter);
    converters.add(0, new MappingJackson2HttpMessageConverter(WireJson.mapper()));
    server = MockRestServiceServer.bindTo(restTemplate).build();
    adapter = new TestAdapter(restTemplate);
  }

  @Test
  void wireShouldCarryTypeDiscriminator() {
    HttpContext context = HttpContext.builder().url("http://127.0.0.1/x")
        .body(Collections.singletonMap("k", "v")).build();
    String httpWire = WireJson.write(context);
    assertTrue(httpWire.contains("\"type\":\"HTTP\""), "报文必须带类型判别符，实际=" + httpWire);

    String noneWire = WireJson.write(new RequestContext());
    assertTrue(noneWire.contains("\"type\":\"NONE\""), "基类的判别符应为 NONE，实际=" + noneWire);
  }

  @Test
  void postShouldReturnTypedResponseWithoutSecondParse() {
    server.expect(requestTo(DISPATCH_URL)).andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("{\"code\":\"0000\",\"message\":\"ok\",\"data\":"
            + "{\"type\":\"HTTP\",\"url\":\"http://127.0.0.1/x\",\"body\":{\"k\":\"v\"}}}",
            MediaType.APPLICATION_JSON));

    ResponseMeta<RequestContext> meta = adapter.doPost(DISPATCH_URL, new RequestContext(), META_CONTEXT);

    server.verify();
    assertNotNull(meta, "响应必须由转换器一次解析成对象，而不是字符串");
    assertEquals("0000", meta.getCode());
    assertTrue(meta.getData() instanceof HttpContext,
        "必须还原成具体子类型，实际=" + (meta.getData() == null ? "null" : meta.getData().getClass()));
    HttpContext context = (HttpContext) meta.getData();
    assertEquals("http://127.0.0.1/x", context.getUrl());
    assertEquals("v", context.getBody().get("k"));
  }

  @Test
  void postForSuccessShouldReflectStatusCode() {
    server.expect(requestTo(DISPATCH_URL)).andRespond(withSuccess());
    assertTrue(adapter.doPostForSuccess(DISPATCH_URL, new RequestContext()));

    server.reset();
    server.expect(requestTo(DISPATCH_URL)).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
    assertFalse(adapter.doPostForSuccess(DISPATCH_URL, new RequestContext()));
  }

  private static class TestAdapter extends BaseEurekaAdapter {

    TestAdapter(RestTemplate restTemplate) {
      super(restTemplate);
    }

    <T> T doPost(String url, Object body, ParameterizedTypeReference<T> type) {
      return post(url, body, type);
    }

    boolean doPostForSuccess(String url, Object body) {
      return postForSuccess(url, body);
    }
  }
}
