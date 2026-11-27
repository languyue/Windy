package com.zj.pipeline.service;

import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.zj.contract.rpc.PageSize;
import com.zj.common.exception.ApiException;
import com.zj.contract.exception.ErrorCode;
import com.zj.contract.git.GitAccessInfo;
import com.zj.common.adapter.git.IGitRepositoryHandler;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.common.utils.OrikaUtil;
import com.zj.common.port.relation.IRelationTargetReader;
import com.zj.common.port.relation.RelationTargetView;
import com.zj.domain.entity.bo.pipeline.CodeChangeBO;
import com.zj.domain.entity.bo.pipeline.RelationDemandBug;
import com.zj.domain.entity.bo.service.MicroserviceBO;
import com.zj.domain.repository.pipeline.ICodeChangeCommandRepository;
import com.zj.domain.repository.service.IMicroServiceQueryRepository;
import com.zj.pipeline.entity.dto.CodeChangeDto;
import com.zj.pipeline.git.RepositoryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2021/10/15
 */
@Slf4j
@Service
public class CodeChangeService {

    private final RepositoryFactory repositoryFactory;
    private final IMicroServiceQueryRepository serviceRepository;
    private final UniqueIdService uniqueIdService;
    private final ICodeChangeCommandRepository codeChangeRepository;
    private final IRelationTargetReader relationTargetReader;

    public CodeChangeService(RepositoryFactory repositoryFactory, IMicroServiceQueryRepository serviceRepository,
                             UniqueIdService uniqueIdService, ICodeChangeCommandRepository codeChangeRepository,
                             IRelationTargetReader relationTargetReader) {
        this.repositoryFactory = repositoryFactory;
        this.serviceRepository = serviceRepository;
        this.uniqueIdService = uniqueIdService;
        this.codeChangeRepository = codeChangeRepository;
        this.relationTargetReader = relationTargetReader;
    }

    public CodeChangeBO getCodeChange(String serviceId, String codeChangeId) {
        Assert.notEmpty(serviceId, "serviceId can not be null");
        return codeChangeRepository.getCodeChange(codeChangeId);
    }

    public String createCodeChange(CodeChangeDto codeChangeDto) {
        GitAccessInfo gitAccessInfo = repositoryFactory.getServiceRepositoryAccessInfo(codeChangeDto.getServiceId());
        IGitRepositoryHandler repository = repositoryFactory.getRepository(gitAccessInfo.getGitType());
        repository.createBranch(codeChangeDto.getChangeBranch(), gitAccessInfo);
        codeChangeDto.setChangeId(uniqueIdService.getUniqueId());
        CodeChangeBO codeChange = OrikaUtil.convert(codeChangeDto, CodeChangeBO.class);
        return codeChangeRepository.saveCodeChange(codeChange) ? codeChange.getChangeId() : "";
    }

    public boolean updateCodeChange(String serviceId, String codeChangeId, CodeChangeDto codeChangeDto) {
        CodeChangeBO codeChangeBO = getCodeChange(serviceId, codeChangeId);
        if (Objects.isNull(codeChangeBO)) {
            throw new ApiException(ErrorCode.NOT_FOUND_CODE_CHANGE);
        }

        CodeChangeBO codeChange = OrikaUtil.convert(codeChangeDto, CodeChangeBO.class);
        codeChange.setChangeId(codeChangeId);
        codeChange.setUpdateTime(System.currentTimeMillis());
        return codeChangeRepository.updateCodeChange(codeChange);
    }

    public PageSize<CodeChangeBO> listCodeChanges(String serviceId, Integer page, Integer size, String name) {
        checkServiceExist(serviceId);
        return codeChangeRepository.getServiceChangesPage(serviceId, page, size, name);
    }

    public Boolean deleteCodeChange(String serviceId, String codeChangeId) {
        CodeChangeBO codeChange = getCodeChange(serviceId, codeChangeId);
        GitAccessInfo gitAccessInfo = repositoryFactory.getServiceRepositoryAccessInfo(serviceId);
        IGitRepositoryHandler repository = repositoryFactory.getRepository(gitAccessInfo.getGitType());
        List<String> branches = repository.listBranch(gitAccessInfo);
        boolean branchExist = branches.stream().anyMatch(branchName -> Objects.equals(branchName, codeChange.getChangeBranch()));
        if (branchExist){
            repository.deleteBranch(codeChange.getChangeBranch(), gitAccessInfo);
        }
        return codeChangeRepository.deleteCodeChange(codeChangeId);
    }

    public List<RelationDemandBug> queryRelationIds(String queryName) {
        try {
            List<RelationTargetView> targets = relationTargetReader.searchByName(queryName);
            return targets.stream().map(target -> new RelationDemandBug(target.getRelationId(),
                    target.getRelationType(), target.getRelationName())).collect(Collectors.toList());
        } catch (Exception e) {
            log.info("query related demand or bug or work error", e);
        }
        return Collections.emptyList();
    }

    private void checkServiceExist(String serviceId) {
        MicroserviceBO serviceDetail = serviceRepository.queryServiceDetail(serviceId);
        if (Objects.isNull(serviceDetail)) {
            log.warn("can not find serviceId ={}", serviceId);
            throw new ApiException(ErrorCode.NOT_FOUND_SERVICE);
        }
    }
}
