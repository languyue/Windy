package com.zj.common.adapter.invoker.eureka;

import com.alibaba.fastjson.JSON;
import com.zj.common.adapter.trace.TidInterceptor;
import com.zj.common.utils.TraceUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

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

    public ResponseEntity<String> requestPost(String url , Object data) {
        HttpEntity<Object> http = new HttpEntity<>(data, requestHeaders());
        try {
            return restTemplate.postForEntity(url, http, String.class);
        } catch (Exception e) {
            log.info("send dispatch task error ={}", e.toString());
        }
        return null;
    }

    public ResponseEntity<String> requestGet(String url) {
        HttpEntity request = new HttpEntity(requestHeaders());
        try {
            return restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        }catch (Exception e){
            log.info("request get request error ", e);
        }
        return null;
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

    protected Response postWithIp(String url, Object data) {
        String traceId = TraceUtils.getTraceId();
        Request request = new Request.Builder().url(url).header(TidInterceptor.HTTP_HEADER_TRACE_ID, traceId)
                .post(RequestBody.create(mediaType, JSON.toJSONString(data))).build();
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
