package com.zj.pipeline.service;

import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.domain.entity.bo.pipeline.BindBranchBO;
import com.zj.domain.entity.bo.pipeline.PipelineBO;
import com.zj.domain.entity.bo.pipeline.PipelineHistoryBO;
import com.zj.domain.entity.bo.pipeline.PublishBindBO;
import com.zj.domain.entity.enums.PipelineType;
import com.zj.domain.repository.pipeline.IBindBranchRepository;
import com.zj.domain.repository.pipeline.IPipelineHistoryRepository;
import com.zj.domain.repository.pipeline.IPublishBindRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @author guyuelan
 * @since 2023/6/28
 */
@Slf4j
@Component
public class PublishService {

    private final IPublishBindRepository publishBindRepository;
    private final IBindBranchRepository bindBranchRepository;
    private final IPipelineHistoryRepository pipelineHistoryRepository;
    private final UniqueIdService uniqueIdService;
    private final PipelineService pipelineService;

    public PublishService(IPublishBindRepository publishBindRepository, IBindBranchRepository bindBranchRepository,
                          IPipelineHistoryRepository pipelineHistoryRepository, UniqueIdService uniqueIdService, PipelineService pipelineService) {
        this.publishBindRepository = publishBindRepository;
        this.bindBranchRepository = bindBranchRepository;
        this.pipelineHistoryRepository = pipelineHistoryRepository;
        this.uniqueIdService = uniqueIdService;
        this.pipelineService = pipelineService;
    }

    public Boolean createPublish(PublishBindBO publishBindBO) {
        BindBranchBO bindBranch = bindBranchRepository.getPipelineBindBranch(publishBindBO.getPipelineId());
        if (Objects.isNull(bindBranch)) {
            log.info("pipeline not bind branch, pipelineId={}", publishBindBO.getPipelineId());
            throw new ApiException(ErrorCode.PIPELINE_NOT_BIND);
        }

        boolean nonePublish = pipelineService.listPipelines(publishBindBO.getServiceId()).stream().noneMatch(pipelineBO -> Objects.equals(
                pipelineBO.getPipelineType(), PipelineType.PUBLISH.getType()));
        if (nonePublish) {
            log.info("service not bind publish pipeline, serviceId={}", publishBindBO.getServiceId());
            throw new ApiException(ErrorCode.SERVICE_CREATE_PUBLISH_PIPELINE);
        }


        PublishBindBO serviceBranch = publishBindRepository.getServiceBranch(publishBindBO.getServiceId(),
                bindBranch.getGitBranch());
        if (Objects.nonNull(serviceBranch)) {
            log.info("service branch publish info exist, serviceId={}, branch={}", publishBindBO.getServiceId(),
                    bindBranch.getGitBranch());
            throw new ApiException(ErrorCode.SERVICE_BRANCH_PUBLISH_EXIST);
        }

        publishBindBO.setBranch(bindBranch.getGitBranch());
        publishBindBO.setPublishId(uniqueIdService.getUniqueId());
        return publishBindRepository.createPublish(publishBindBO);
    }

    public Boolean updatePublish(PublishBindBO publishBind) {
        return publishBindRepository.createPublish(publishBind);
    }

    public List<PublishBindBO> getPublishes(String serviceId) {
        return publishBindRepository.getServicePublishes(serviceId);
    }

    public boolean deletePublish(String publishId) {
        PublishBindBO publish = publishBindRepository.getPublishById(publishId);
        String pipelineId = publish.getPipelineId();
        PipelineHistoryBO latestPipelineHistory = pipelineHistoryRepository.getLatestPipelineHistory(pipelineId);
        if (Objects.nonNull(latestPipelineHistory) && Objects.equals(latestPipelineHistory.getPipelineStatus(),
                ProcessStatus.RUNNING.getType())) {
            log.info("pipeline is running , can not delete publish={}", publishId);
            throw new ApiException(ErrorCode.PIPELINE_RUNNING_NOT_DELETE_PUBLISH);
        }
        return publishBindRepository.deletePublish(publishId);
    }
}
