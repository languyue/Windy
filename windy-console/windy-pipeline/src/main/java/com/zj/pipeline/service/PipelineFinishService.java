package com.zj.pipeline.service;

import com.zj.common.entity.event.CodeChangePublishedEvent;
import com.zj.common.entity.event.PipelineFinishedEvent;
import com.zj.domain.entity.bo.pipeline.CodeChangeBO;
import com.zj.domain.entity.bo.pipeline.PublishBindBO;
import com.zj.domain.repository.pipeline.ICodeChangeCommandRepository;
import com.zj.domain.repository.pipeline.IPublishBindCommandRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 发布结束后的业务域收尾。
 *
 * 这段逻辑原先在 master 的 PublishEndListener 里，跨进程直接写业务域与发布绑定表；
 * 现在 master 只发事件，收尾由业务域自己完成。
 *
 * @author guyuelan
 */
@Slf4j
@Service
public class PipelineFinishService {

    private final IPublishBindCommandRepository publishBindRepository;
    private final ICodeChangeCommandRepository codeChangeRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PipelineFinishService(IPublishBindCommandRepository publishBindRepository,
        ICodeChangeCommandRepository codeChangeRepository,
        ApplicationEventPublisher eventPublisher) {
        this.publishBindRepository = publishBindRepository;
        this.codeChangeRepository = codeChangeRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 处理 master 宣告的发布结束事件
     *
     * @param event 结束事件
     * @return 是否处理完成
     */
    public boolean handlePublishFinished(PipelineFinishedEvent event) {
        if (Objects.isNull(event) || Objects.isNull(event.getServiceId())) {
            log.info("pipeline finished event has no serviceId, ignore");
            return false;
        }

        //获取流水线所有的发布分支
        List<PublishBindBO> pipelinePublishes = publishBindRepository.getServicePublishes(event.getServiceId());
        List<String> branches =
                pipelinePublishes.stream().map(PublishBindBO::getBranch).collect(Collectors.toList());
        log.info("get publish branches ={}", branches);

        //根据发布分支查询的变更列表，然后更新需求或缺陷状态
        List<CodeChangeBO> serviceChangeList = codeChangeRepository.getServiceChanges(event.getServiceId());
        finishCodeRelatedResource(serviceChangeList, branches);

        //删除已发布的变更列表
        List<String> serviceChanges = serviceChangeList.stream()
                .filter(codeChange -> branches.contains(codeChange.getChangeBranch()))
                .map(CodeChangeBO::getChangeId).collect(Collectors.toList());
        log.info("code changes id ={}", serviceChanges);
        boolean batchDeleteCodeChange = codeChangeRepository.batchDeleteCodeChange(serviceChanges);

        //删除发布绑定的分支
        boolean deletePublishLine = publishBindRepository.deleteServicePublishes(event.getServiceId());
        log.info("delete code change result = {} delete publish result={}", batchDeleteCodeChange, deletePublishLine);
        return true;
    }

    private void finishCodeRelatedResource(List<CodeChangeBO> serviceChangeList, List<String> publishBranches) {
        if (CollectionUtils.isEmpty(serviceChangeList)) {
            log.info("service code change is empty not finish relation source");
            return;
        }

        //获取已发布的分支关联的变更，按关联类型分组后发事件，由业务域自己推进状态
        Map<Integer, List<String>> relationMap = serviceChangeList.stream()
                .filter(codeChange -> publishBranches.contains(codeChange.getChangeBranch()))
                .collect(Collectors.groupingBy(CodeChangeBO::getRelationType,
                        Collectors.mapping(CodeChangeBO::getRelationId, Collectors.toList())));
        CodeChangePublishedEvent publishedEvent = new CodeChangePublishedEvent();
        publishedEvent.setRelationMap(relationMap);
        publishedEvent.setPublishBranches(publishBranches);
        eventPublisher.publishEvent(publishedEvent);
    }
}
