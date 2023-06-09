package com.zj.client.service;

import com.alibaba.fastjson.JSONObject;
import com.zj.client.entity.dto.BuildParam;
import com.zj.client.entity.dto.ResponseModel;
import com.zj.client.pipeline.git.GitOperator;
import com.zj.client.pipeline.maven.MavenOperator;
import com.zj.common.enums.ProcessStatus;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author guyuelan
 * @since 2023/3/29
 */
@Slf4j
@Service
public class CodeBuildService {

  @Value("windy.pipeline.git.workspace:/opt/windy/")
  private String workspace;
  @Autowired
  private GitOperator gitOperator;

  @Autowired
  private MavenOperator mavenOperator;

  private Map<String, Integer> statusMap = new ConcurrentHashMap<>();

  private ExecutorService executorService = new ThreadPoolExecutor(10, 20, 30, TimeUnit.MINUTES,
      new LinkedBlockingQueue<>(100), new CallerRunsPolicy());

  public Boolean buildCode(BuildParam buildParam) {
    executorService.execute(() -> {
      try {
        //从git服务端拉取代码
        gitOperator.pullCodeFromGit(buildParam.getGitUrl(), buildParam.getBranch());
        String pomPath = getTargetPomPath(buildParam.getGitUrl(), buildParam.getPomPath());
        Integer exitCode = mavenOperator.build(pomPath);
        log.info("get maven exit code={}", exitCode);
        statusMap.put(buildParam.getRecordId(), ProcessStatus.SUCCESS.getType());
      } catch (Exception e) {
        log.error("buildCode error", e);
        statusMap.put(buildParam.getRecordId(), ProcessStatus.FAIL.getType());
      }
    });

    statusMap.put(buildParam.getRecordId(), ProcessStatus.RUNNING.getType());
    return true;
  }

  private String getTargetPomPath(String gitUrl, String configPath) {
    String serviceName = GitOperator.getServiceFromUrl(gitUrl);
    return workspace + File.separator + serviceName + File.separator + configPath;
  }

  public ResponseModel getRecordStatus(String recordId) {
    Integer status = statusMap.get(recordId);
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("status", status);
    ResponseModel responseModel = new ResponseModel();
    responseModel.setStatus(status);
    responseModel.setMessage("构建状态查询");
    responseModel.setData(jsonObject);
    return responseModel;
  }
}
