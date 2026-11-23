package com.zj.common.adapter.invoker.eureka;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zj.common.adapter.trace.TidInterceptor;
import com.zj.common.utils.TraceUtils;
import com.zj.contract.json.ContractJson;
import com.zj.contract.json.WireJson;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BaseEurekaAdapter {

    private final RestTemplate restTemplate;
    private final okhttp3.MediaType mediaType = okhttp3.MediaType.get("application/json; charset=utf-8");
    protected final OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(60,
            TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).build();

    public BaseEurekaAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 每次请求构造独立的headers，保证traceId是当前调用线程的
     * 如果使用singleton成员变量，traceId会被固定成首次请求的值
     * */
    private HttpHeaders requestHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set(TidInterceptor.HTTP_HEADER_TRACE_ID, TraceUtils.getTraceId());
        return headers;
    }

    /**
     * 直接按目标类型收发消息，序列化与反序列化交给消息转换器一次完成，
     * 调用方不再拿到字符串后手工 parse。
     *
     * @param url    请求地址
     * @param method 请求方法
     * @param body   请求体，GET/DELETE 传 null
     * @param type   期望的响应类型
     * @return 响应体，失败返回 null
     */
    protected <T> T exchange(String url, HttpMethod method, Object body, ParameterizedTypeReference<T> type) {
        HttpEntity<Object> request = new HttpEntity<>(body, requestHeaders());
        try {
            return restTemplate.exchange(url, method, request, type).getBody();
        } catch (Exception e) {
            log.error("exchange request error method={} url={}", method, url, e);
        }
        return null;
    }

    /**
     * 类型化的 POST：序列化交给消息转换器，调用方直接拿到目标对象
     */
    protected <T> T post(String url, Object body, ParameterizedTypeReference<T> type) {
        return exchange(url, HttpMethod.POST, body, type);
    }

    /**
     * 类型化的 GET
     */
    protected <T> T get(String url, ParameterizedTypeReference<T> type) {
        return exchange(url, HttpMethod.GET, null, type);
    }

    /**
     * 只关心是否送达的 POST：响应体在转换器层就丢弃，不再把报文当字符串带进业务代码
     *
     * @param url  请求地址
     * @param body 请求体
     * @return 响应是否 2xx
     */
    protected boolean postForSuccess(String url, Object body) {
        HttpEntity<Object> request = new HttpEntity<>(body, requestHeaders());
        try {
            return restTemplate.exchange(url, HttpMethod.POST, request, Void.class).getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("post request error url={}", url, e);
        }
        return false;
    }

    /**
     * 直连 IP 的 OkHttp 通道：响应体在这里按与 RestTemplate 相同的 wire 语义解析成对象，
     * 业务侧不再拿到字符串后自己 parse。
     */
    protected <T> T readEntity(Response response, ParameterizedTypeReference<T> type) throws IOException {
        String responseBody = response.body().string();
        log.info("response body = {}", responseBody);
        ObjectMapper mapper = WireJson.mapper();
        JavaType javaType = mapper.getTypeFactory().constructType(type.getType());
        return mapper.readValue(responseBody, javaType);
    }

    protected Response postWithIp(String url, Object data) {
        String traceId = TraceUtils.getTraceId();
        Request request = new Request.Builder().url(url).header(TidInterceptor.HTTP_HEADER_TRACE_ID, traceId)
                .post(RequestBody.create(mediaType, ContractJson.write(data))).build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            log.info("notify master ip status result code={}", response.code());
            return response;
        } catch (Exception e) {
            log.error("request post with ip error ={}", e.toString());
        }
        return null;
    }

    protected Response getWithIp(String url) {
        String traceId = TraceUtils.getTraceId();
        Request request =
                new Request.Builder().url(url).get().header(TidInterceptor.HTTP_HEADER_TRACE_ID, traceId).build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            log.info(" get response code={}", response.code());
            return response;
        } catch (Exception e) {
            log.error("request get with ip error ={}", e.toString());
        }
        return null;
    }
}
