package com.zj.common.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * OkHttpUtil
 */
@Slf4j
public final class OkHttpUtil {

    private OkHttpUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final OkHttpClient client =
            new OkHttpClient.Builder()
                    .readTimeout(5, TimeUnit.SECONDS)       // 设置超时时间
                    .build();


    public static final MediaType MEDIA_TYPE_JSON = MediaType.get("application/json; charset=utf-8");


    /**
     * 发起Get请求
     *
     * @param url    url
     * @param params 参数
     * @return 响应结果
     */
    public static String doGet(String url, Map<String, Object> params) {
        Call call = createGetCall(url, params);
        return execute(call);
    }


    /**
     * 发起Get请求
     *
     * @param url    url
     * @param params 参数
     * @return HttpResponse 响应结果
     */
    public static HttpResponse doGetWithResponse(String url, Map<String, Object> params) {
        Call call = createGetCall(url, params);
        return executeWithResponse(call);
    }


    /**
     * 发起 Post请求, 使用form表单参数
     *
     * @param url    url
     * @param params 参数
     * @return 响应结果
     */
    public static String doPost(String url, Map<String, Object> params) {
        Call call = createPostCall(url, params);
        return execute(call);
    }

    /**
     * 发起Post请求
     *
     * @param url    url
     * @param params 参数
     * @return HttpResponse 响应结果
     */
    public static HttpResponse doPostWithResponse(String url, Map<String, Object> params) {
        Call call = createPostCall(url, params);
        return executeWithResponse(call);
    }


    /**
     * 发起 post请求, 使用json参数
     *
     * @param url  url
     * @param json json参数
     * @return resp
     */
    public static String doPost(String url, String json) {
        Call call = createPostJsonCall(url, json);
        return execute(call);
    }

    /**
     * 发起 post请求, 使用json参数
     *
     * @param url  url
     * @param json json参数
     * @return resp
     */
    public static HttpResponse doPostWithResponse(String url, String json) {
        Call call = createPostJsonCall(url, json);
        return executeWithResponse(call);
    }



    /**
     * [异步] 发起Get请求
     *
     * @param url      url
     * @param params   参数
     * @param callback 回调方法
     */
    public static void doGetAsync(String url, Map<String, Object> params, Callback callback) {
        Call call = createGetCall(url, params);
        call.enqueue(callback);
    }

    /**
     * [异步] 发起 Post请求
     *
     * @param url    url
     * @param params 参数
     */
    public static void doPostAsync(String url, Map<String, Object> params, Callback callback) {
        Call call = createPostCall(url, params);
        call.enqueue(callback);
    }

    /**
     * [异步] 发起 post请求, 使用json参数
     *
     * @param url  url
     * @param json json参数
     */
    public static void doPostAsync(String url, String json, Callback callback) {
        Call call = createPostJsonCall(url, json);
        call.enqueue(callback);
    }


    /**
     * Get请求, 构造 Call对象
     *
     * @param url    请求url
     * @param params 请求参数
     * @return Call
     */
    private static Call createGetCall(String url, Map<String, Object> params) {

        Request.Builder builder = new Request.Builder();
        Request request = builder.url(url).build();
        // 设置参数
        HttpUrl httpUrl = createHttpUrl(request, params);
        builder.url(httpUrl).build();

        return client.newCall(builder.build());
    }

    /**
     * Post请求, 构造 Call对象
     *
     * @param url    请求url
     * @param params 请求参数
     * @return Call
     */
    private static Call createPostCall(String url, Map<String, Object> params) {
        Request request = new Request.Builder()
                .post(createFormBody(params))
                .url(url)
                .build();
        return client.newCall(request);
    }

    /**
     * Post请求, 构造 Call对象
     *
     * @param url  请求url
     * @param json 请求参数
     * @return Call
     */
    private static Call createPostJsonCall(String url, String json) {
        Request request = new Request.Builder()
                .post(RequestBody.create(MEDIA_TYPE_JSON, json))
                .url(url)
                .build();
        return client.newCall(request);
    }


    /**
     * 为 get请求设置参数
     *
     * @param request request对象
     * @param params  请求参数
     * @return 设置了参数的 HttpUrl对象
     */
    private static HttpUrl createHttpUrl(Request request, Map<String, Object> params) {
        HttpUrl.Builder urlBuilder = request.url().newBuilder();
        if (params != null && params.size() > 0) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                urlBuilder.addQueryParameter(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        return urlBuilder.build();
    }


    /**
     * 为 post请求设置参数
     *
     * @param params 请求参数
     * @return FormBody
     */
    private static FormBody createFormBody(Map<String, Object> params) {
        FormBody.Builder builder = new FormBody.Builder();
        if (params != null && params.size() > 0) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                builder.add(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        return builder.build();
    }


    /**
     * 同步执行 http请求
     *
     * @param call call对象
     * @return 响应结果
     */
    public static String execute(Call call) {
        String respStr = null;
        try (Response response = call.execute()) {
            if (response.body() != null) {
                respStr = response.body().string();
            }
        } catch (IOException e) {
            log.error("{");
        }
        return respStr;
    }

    /**
     * 同步执行 http请求
     *
     * @param call call对象
     * @return HttpResponse 响应结果
     */
    public static HttpResponse executeWithResponse(Call call) {
        try (Response response = call.execute()) {
            String body = "";
            if (response.body() != null) {
                body = response.body().string();
            }

            return new HttpResponse(
                    response.code(),
                    body,
                    response.isSuccessful(),
                    response.message());
        } catch (IOException e) {
            HttpResponse errorResponse = new HttpResponse(0, "", false, e.getMessage());
            errorResponse.setErrorMessage("Network error: " + e.getMessage());
            return errorResponse;
        }
    }


    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class HttpResponse {
        private int statusCode;
        private String body;
        private boolean successful;
        private String errorMessage;
        private Map<String, String> headers;

        public HttpResponse(int statusCode, String body, boolean successful, String errorMessage) {
            this.statusCode = statusCode;
            this.body = body;
            this.successful = successful;
            this.errorMessage = errorMessage;
        }
    }
}
