package com.zj.pipeline.git;

import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.domain.entity.vo.GitAccessVo;
import com.zj.domain.repository.pipeline.ISystemConfigRepository;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

/**
 * @author guyuelan
 * @since 2023/3/10
 */
@Slf4j
@Component
public class GitRequestProxy {

  public static final MediaType CONTENT_TYPE = MediaType.parse("application/json");
  OkHttpClient okHttpClient = new OkHttpClient.Builder().readTimeout(Duration.ofMinutes(1))
      .connectTimeout(Duration.ofSeconds(30)).build();

  private final ISystemConfigRepository systemRepository;

  public GitRequestProxy(ISystemConfigRepository systemRepository) {
    this.systemRepository = systemRepository;
  }

  public GitAccessVo getGitAccess() {
    return systemRepository.getGitAccess();
  }

  public String get(String path, Map<String, String> headers) {
    String gitDomain = getGitAccess().getGitDomain();
    Request request = new Request.Builder().url(gitDomain + path).headers(Headers.of(headers)).get()
        .build();
    try {
      Response execute = okHttpClient.newCall(request).execute();
      return execute.body().string();
    } catch (IOException e) {
      throw new ApiException(ErrorCode.REQUEST_GIT_SERVER_FAILED);
    }
  }

  public Response getWithResponse(String path, Map<String, String> headers) {
    String gitDomain = getGitAccess().getGitDomain();
    Request request = new Request.Builder().url(gitDomain + path).headers(Headers.of(headers)).get()
            .build();
    try {
      Response execute = okHttpClient.newCall(request).execute();
      return execute;
    } catch (IOException e) {
      throw new ApiException(ErrorCode.REQUEST_GIT_SERVER_FAILED);
    }
  }

  public String post(String path, String body, Map<String, String> headers) {
    RequestBody requestBody = RequestBody.create(CONTENT_TYPE, body);
    String gitDomain = getGitAccess().getGitDomain();
    Request request = new Request.Builder().url(gitDomain + path).headers(Headers.of(headers))
        .post(requestBody).build();
    try {
      Response execute = okHttpClient.newCall(request).execute();
      return execute.body().string();
    } catch (IOException e) {
      throw new ApiException(ErrorCode.REQUEST_GIT_SERVER_FAILED);
    }
  }

  public String put(String path, String body) {
    RequestBody requestBody = RequestBody.create(CONTENT_TYPE, body);
    String gitDomain = getGitAccess().getGitDomain();
    Request request = new Request.Builder().url(gitDomain + path).put(requestBody).build();
    try {
      Response execute = okHttpClient.newCall(request).execute();
      return execute.body().string();
    } catch (IOException e) {
      throw new ApiException(ErrorCode.REQUEST_GIT_SERVER_FAILED);
    }
  }

  public String delete(String path, Map<String, String> headers) {
    String gitDomain = getGitAccess().getGitDomain();
    Request request = new Request.Builder().url(gitDomain + path).headers(Headers.of(headers))
        .delete().build();
    try {
      Response execute = okHttpClient.newCall(request).execute();
      return execute.body().string();
    } catch (IOException e) {
      log.error("request git serve error", e);
      throw new ApiException(ErrorCode.REQUEST_GIT_SERVER_FAILED);
    }
  }
}
