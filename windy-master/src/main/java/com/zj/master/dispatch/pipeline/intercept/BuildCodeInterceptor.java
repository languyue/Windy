package com.zj.master.dispatch.pipeline.intercept;

import com.zj.contract.pipeline.PipelineConfig;
import com.zj.contract.pipeline.ServiceConfig;
import com.zj.contract.pipeline.ServiceContext;
import com.zj.common.enums.ExecuteType;
import com.zj.common.exception.ApiException;
import com.zj.contract.exception.ErrorCode;
import com.zj.domain.entity.bo.pipeline.BindBranchBO;
import com.zj.domain.entity.bo.pipeline.PipelineBO;
import com.zj.domain.entity.bo.pipeline.PipelineNodeBO;
import com.zj.domain.entity.bo.pipeline.PublishBindBO;
import com.zj.domain.entity.bo.service.MicroserviceBO;
import com.zj.domain.entity.enums.PipelineType;
import com.zj.domain.entity.vo.ImageRepositoryVo;
import com.zj.domain.repository.pipeline.IBindBranchQueryRepository;
import com.zj.domain.repository.pipeline.IPipelineNodeQueryRepository;
import com.zj.domain.repository.pipeline.IPipelineQueryRepository;
import com.zj.domain.repository.pipeline.IPublishBindQueryRepository;
import com.zj.domain.repository.pipeline.ISystemConfigQueryRepository;
import com.zj.domain.repository.service.IMicroServiceQueryRepository;
import com.zj.contract.pipeline.node.BuildCodeContext;
import com.zj.contract.pipeline.node.TaskNode;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/6/15
 */
@Component
public class BuildCodeInterceptor implements INodeExecuteInterceptor {

    private final IPipelineNodeQueryRepository pipelineNodeRepository;
    private final IPublishBindQueryRepository publishBindRepository;
    private final IPipelineQueryRepository pipelineRepository;
    private final IBindBranchQueryRepository gitBindRepository;
    private final ISystemConfigQueryRepository configRepository;
    private final IMicroServiceQueryRepository microServiceRepository;

    public BuildCodeInterceptor(IPipelineNodeQueryRepository pipelineNodeRepository,
                                IPublishBindQueryRepository publishBindRepository, IPipelineQueryRepository pipelineRepository,
                                IBindBranchQueryRepository gitBindRepository, ISystemConfigQueryRepository configRepository,
                                IMicroServiceQueryRepository microServiceRepository) {
        this.pipelineNodeRepository = pipelineNodeRepository;
        this.publishBindRepository = publishBindRepository;
        this.pipelineRepository = pipelineRepository;
        this.gitBindRepository = gitBindRepository;
        this.configRepository = configRepository;
        this.microServiceRepository = microServiceRepository;
    }

    @Override
    public int sort() {
        return 2;
    }

    @Override
    public void beforeExecute(TaskNode taskNode) {
        if (!Objects.equals(taskNode.getExecuteType(), ExecuteType.BUILD.name())) {
            return;
        }

        ImageRepositoryVo repository = configRepository.getImageRepository();
        PipelineNodeBO pipelineNode = pipelineNodeRepository.getPipelineNode(taskNode.getNodeId());
        PipelineBO pipeline = pipelineRepository.getPipeline(pipelineNode.getPipelineId());
        MicroserviceBO serviceDetail = microServiceRepository.queryServiceDetail(pipeline.getServiceId());
        ServiceConfig serviceConfig = serviceDetail.getServiceConfig();
        if (Objects.equals(pipeline.getPipelineType(), PipelineType.PUBLISH.getType())) {
            //如果是发布流水线，则要查询发布的流水线分支
            List<PublishBindBO> servicePublishes = publishBindRepository.getServicePublishes(pipeline.getServiceId());
            List<String> branches =
                    servicePublishes.stream().map(PublishBindBO::getBranch).collect(Collectors.toList());
            PipelineConfig pipelineConfig = pipeline.getPipelineConfig();
            String version = null;
            if (Objects.nonNull(pipelineConfig) && MapUtils.isNotEmpty(pipelineConfig.getParamList())) {
                version =
                        Optional.ofNullable(pipelineConfig.getParamList().get("version")).map(String::valueOf).orElse(null);
            }
            rebuildRequestContext(taskNode, branches, repository, true, serviceConfig, version);
            return;
        }

        BindBranchBO bindBranch = gitBindRepository.getPipelineBindBranch(pipelineNode.getPipelineId());
        if (Objects.isNull(bindBranch)) {
            throw new ApiException(ErrorCode.NOT_FIND_BRANCH);
        }
        rebuildRequestContext(taskNode, Collections.singletonList(bindBranch.getGitBranch()), repository, false,
                serviceConfig, null);
    }

    private void rebuildRequestContext(TaskNode taskNode, List<String> branches, ImageRepositoryVo repository,
                                       boolean isPublish, ServiceConfig serviceConfig, String version) {
        BuildCodeContext buildCodeContext = (BuildCodeContext) taskNode.getRequestContext();
        buildCodeContext.setVersion(version);
        buildCodeContext.setBranches(branches);
        buildCodeContext.setIsPublish(isPublish);
        buildCodeContext.setServiceName(serviceConfig.getAppName());
        Optional.ofNullable(serviceConfig.getServiceContext()).ifPresent(context ->{
            buildCodeContext.setDeployType(context.getDeployType());
            buildCodeContext.setCode(context.getCode());
            buildCodeContext.setBuildVersion(context.getBuildVersion());
            buildCodeContext.setMainBranch(context.getMainBranch());
        });
        if (Objects.nonNull(repository)) {
            buildCodeContext.setUser(repository.getUserName());
            buildCodeContext.setPassword(repository.getPassword());
            buildCodeContext.setRepository(repository.getRepositoryUrl());
        }
        taskNode.setRequestContext(buildCodeContext);
    }
}
