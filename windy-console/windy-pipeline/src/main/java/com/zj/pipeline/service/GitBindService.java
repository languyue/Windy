package com.zj.pipeline.service;

import com.zj.common.adapter.git.CommitMessage;
import com.zj.common.entity.event.CodePushedEvent;
import com.zj.contract.git.GitAccessInfo;
import com.zj.common.adapter.git.IGitRepositoryHandler;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.common.exception.ApiException;
import com.zj.contract.exception.ErrorCode;
import com.zj.domain.entity.bo.pipeline.BindBranchBO;
import com.zj.domain.entity.bo.pipeline.CodeChangeBO;
import com.zj.domain.entity.bo.pipeline.PipelineBO;
import com.zj.domain.repository.pipeline.IBindBranchCommandRepository;
import com.zj.domain.repository.pipeline.ICodeChangeQueryRepository;
import com.zj.pipeline.entity.enums.GitEventType;
import com.zj.pipeline.entity.vo.GitPushResultVo;
import com.zj.pipeline.git.RepositoryFactory;
import com.zj.pipeline.git.hook.IGitWebhook;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2021/10/15
 */
@Slf4j
@Service
public class GitBindService {

    private final PipelineService pipelineService;
    private final RepositoryFactory repositoryFactory;
    private final UniqueIdService uniqueIdService;
    private final IBindBranchCommandRepository gitBindRepository;
    private final ICodeChangeQueryRepository codeChangeRepository;
    private final Map<String, IGitWebhook> webhookMap;
    private final ApplicationEventPublisher eventPublisher;
    private final Executor executorService;

    public GitBindService(PipelineService pipelineService, RepositoryFactory repositoryFactory,
                          UniqueIdService uniqueIdService, IBindBranchCommandRepository gitBindRepository,
                          ICodeChangeQueryRepository codeChangeRepository, List<IGitWebhook> webhooks,
                          ApplicationEventPublisher eventPublisher,
                          @Qualifier("webHookExecutorPool") Executor executorService) {
        this.pipelineService = pipelineService;
        this.repositoryFactory = repositoryFactory;
        this.uniqueIdService = uniqueIdService;
        this.gitBindRepository = gitBindRepository;
        this.codeChangeRepository = codeChangeRepository;
        webhookMap = webhooks.stream().collect(Collectors.toMap(IGitWebhook::platform, webhook -> webhook));
        this.eventPublisher = eventPublisher;
        this.executorService = executorService;
    }

    public String createGitBind(BindBranchBO bindBranchBO) {
        List<BindBranchBO> bindDtoList = listGitBinds(bindBranchBO.getPipelineId());
        Optional<BindBranchBO> optional = bindDtoList.stream()
                .filter(gitBind -> Objects.equals(gitBind.getGitBranch(), bindBranchBO.getGitBranch()))
                .findAny();
        if (optional.isPresent()) {
            throw new ApiException(ErrorCode.BRANCH_ALREADY_BIND);
        }

        bindBranchBO.setBindId(uniqueIdService.getUniqueId());
        boolean result = gitBindRepository.saveGitBranch(bindBranchBO);
        return result ? bindBranchBO.getBindId() : "";
    }

    public List<BindBranchBO> listGitBinds(String pipelineId) {
        checkPipelineExist(pipelineId);
        return gitBindRepository.getPipelineRelatedBranches(pipelineId);
    }

    @Transactional
    public Boolean updateGitBind(BindBranchBO bindBranchBO) {
        checkPipelineExist(bindBranchBO.getPipelineId());

        //解绑其他分支
        List<BindBranchBO> branches = gitBindRepository.getPipelineRelatedBranches(
                bindBranchBO.getPipelineId());
        List<String> unbindBranches = branches.stream().filter(
                        branch -> branch.getIsChoose() && !Objects.equals(branch.getGitBranch(),
                                bindBranchBO.getGitBranch())).map(BindBranchBO::getBindId)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(unbindBranches)) {
            gitBindRepository.batchUnbindBranches(unbindBranches);
        }

        BindBranchBO gitBind = getGitBind(bindBranchBO.getBindId());
        if (Objects.isNull(gitBind)) {
            throw new ApiException(ErrorCode.NOT_FOUND_PIPELINE_GIT_BIND);
        }
        return gitBindRepository.updateGitBranch(bindBranchBO);
    }

    private BindBranchBO getGitBind(String bindId) {
        return gitBindRepository.getGitBranch(bindId);
    }

    public Boolean deleteGitBind(String pipelineId, String bindId) {
        checkPipelineExist(pipelineId);
        return gitBindRepository.deleteGitBranch(bindId);
    }

    private void checkPipelineExist(String pipelineId) {
        PipelineBO pipelineBO = pipelineService.getPipeline(pipelineId);
        if (Objects.isNull(pipelineBO)) {
            throw new ApiException(ErrorCode.NOT_FOUND_PIPELINE);
        }
    }

    public boolean notifyHook(String platform, HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            log.info("get request header name={} value={}",name, request.getHeader(name));
        }
        IGitWebhook gitWebhook = webhookMap.get(platform);
        String bodyString = getBodyString(request);
        GitPushResultVo gitPushResultVo = gitWebhook.webhook(bodyString, request);
        executorService.execute(() -> updateProcessStatusIfNeed(gitPushResultVo));
        return true;
    }
    private String getBodyString(HttpServletRequest request){
        // 读取请求体内容
        StringBuilder payload = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                payload.append(line);
            }
            return payload.toString();
        }catch (Exception e){
            log.info("get request body error", e);
        }
        return null;
    }

    private void updateProcessStatusIfNeed(GitPushResultVo gitPushResultVo) {
        try {
            if (Objects.nonNull(gitPushResultVo) && Objects.equals(gitPushResultVo.getEventType(), GitEventType.PUSH.getType())) {
                List<CodeChangeBO> codeChanges = codeChangeRepository.getServiceChanges(gitPushResultVo.getRelatedServiceId());
                //只宣告哪些关联资源随本次推送进入研发中，状态怎么变由业务域自己决定
                Map<Integer, List<String>> relationMap = codeChanges.stream()
                        .collect(Collectors.groupingBy(CodeChangeBO::getRelationType,
                                Collectors.mapping(CodeChangeBO::getRelationId, Collectors.toList())));
                CodePushedEvent pushedEvent = new CodePushedEvent();
                pushedEvent.setServiceId(gitPushResultVo.getRelatedServiceId());
                pushedEvent.setRelationMap(relationMap);
                eventPublisher.publishEvent(pushedEvent);
            }
        }catch (Exception e){
            log.info("update status error, after code push", e);
        }
    }


    public List<String> getServiceBranch(String serviceId) {
        GitAccessInfo gitAccessInfo = repositoryFactory.getServiceRepositoryAccessInfo(serviceId);
        IGitRepositoryHandler repository = repositoryFactory.getRepository(gitAccessInfo.getGitType());
        return repository.listBranch(gitAccessInfo);
    }

    public List<CommitMessage> getBranchCommits(String serviceId, String branchName) {
        GitAccessInfo gitAccessInfo = repositoryFactory.getServiceRepositoryAccessInfo(serviceId);
        IGitRepositoryHandler repository = repositoryFactory.getRepository(gitAccessInfo.getGitType());
        return repository.getBranchCommits(branchName, gitAccessInfo);
    }
}
