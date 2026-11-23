package com.zj.pipeline.git.hook;

import com.zj.contract.git.GitAccessInfo;
import com.zj.common.exception.ApiException;
import com.zj.contract.exception.ErrorCode;
import com.zj.domain.entity.bo.pipeline.BindBranchBO;
import com.zj.domain.entity.bo.pipeline.PipelineBO;
import com.zj.domain.entity.bo.service.MicroserviceBO;
import com.zj.domain.repository.pipeline.IBindBranchQueryRepository;
import com.zj.domain.repository.pipeline.ISystemConfigQueryRepository;
import com.zj.domain.repository.service.IMicroServiceQueryRepository;
import com.zj.pipeline.entity.enums.PipelineExecuteType;
import com.zj.pipeline.entity.vo.GitPushResultVo;
import com.zj.pipeline.service.PipelineService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/6/27
 */
@Slf4j
public abstract class AbstractWebhook implements IGitWebhook {

  private final IMicroServiceQueryRepository serviceRepository;
  private final PipelineService pipelineService;
  private final IBindBranchQueryRepository gitBindRepository;
  private final Executor executorService;
  private final ISystemConfigQueryRepository systemConfigRepository;

  protected AbstractWebhook(IMicroServiceQueryRepository serviceRepository, PipelineService pipelineService,
                            Executor executorService, IBindBranchQueryRepository gitBindRepository, ISystemConfigQueryRepository systemConfigRepository) {
    this.serviceRepository = serviceRepository;
    this.pipelineService = pipelineService;
    this.gitBindRepository = gitBindRepository;
    this.executorService = executorService;
    this.systemConfigRepository = systemConfigRepository;
  }

  @Override
  public GitPushResultVo webhook(String data, HttpServletRequest request) {
    GitPushResultVo gitPushResultVo = analyzeData(data, request);
    if (Objects.isNull(gitPushResultVo) ||StringUtils.isEmpty(gitPushResultVo.getBranch())
            || StringUtils.isEmpty(gitPushResultVo.getRepository())) {
      log.info("receive git push event but analyze data error");
      return null;
    }

    MicroserviceBO microservice = serviceRepository.getServiceByGitUrl(gitPushResultVo.getRepository());
    if (Objects.isNull(microservice)) {
      log.info("can not find service by git url={}", gitPushResultVo.getRepository());
      return null;
    }
    //如果没有流水线执行就直接退出
    gitPushResultVo.setRelatedServiceId(microservice.getServiceId());
    List<PipelineBO> pushPipelines = getServicePipelineByType(microservice, gitPushResultVo, PipelineExecuteType.PUSH);
    if (CollectionUtils.isEmpty(pushPipelines)){
      return gitPushResultVo;
    }

    //根据当前推送的分支查找关联的流水线，然后执行
    pushPipelines.forEach(pipeline -> executorService.execute(() -> {
      List<BindBranchBO> gitBinds = listGitBinds(pipeline.getPipelineId());
      Optional<BindBranchBO> optional = gitBinds.stream().filter(BindBranchBO::getIsChoose)
          .filter(gitBind -> Objects.equals(gitBind.getGitBranch(), gitPushResultVo.getBranch()))
          .findAny();
      if (optional.isPresent()) {
        pipelineService.execute(pipeline.getPipelineId());
        log.info("web hook trigger pipeline execute pipeline={}", pipeline.getPipelineId());
      }
    }));
    return gitPushResultVo;
  }

  private List<PipelineBO> getServicePipelineByType(MicroserviceBO microservice, GitPushResultVo parseResult,
                                                    PipelineExecuteType executeType) {
    List<PipelineBO> pipelines = pipelineService.getServicePipelines(microservice.getServiceId());
    if (CollectionUtils.isEmpty(pipelines)) {
      log.info("can not find pipelines service={}", parseResult.getRepository());
      return Collections.emptyList();
    }

    List<PipelineBO> pushPipelines = pipelines.stream().filter(
            pipeline -> Objects.equals(executeType.getType(), pipeline.getExecuteType()))
        .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(pushPipelines)) {
      log.info("not find pushed pipelines service={} serviceId={}", microservice.getServiceName(),
          microservice.getServiceId());
      return Collections.emptyList();
    }
    return pushPipelines;
  }

  public abstract GitPushResultVo analyzeData(String data, HttpServletRequest request);

  public GitAccessInfo getGitAccessInfo(){
    return systemConfigRepository.getGitAccess();
  }
  public List<BindBranchBO> listGitBinds(String pipelineId) {
    checkPipelineExist(pipelineId);
    return gitBindRepository.getPipelineRelatedBranches(pipelineId);
  }

  private void checkPipelineExist(String pipelineId) {
    PipelineBO pipelineBO = pipelineService.getPipeline(pipelineId);
    if (Objects.isNull(pipelineBO)) {
      throw new ApiException(ErrorCode.NOT_FOUND_PIPELINE);
    }
  }

  protected String getBranchFromHookData(String ref) {
    int index = ref.lastIndexOf("/");
    return ref.substring(index + 1);
  }
}
