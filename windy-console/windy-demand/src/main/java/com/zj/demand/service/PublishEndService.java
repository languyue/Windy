package com.zj.demand.service;

import com.zj.domain.entity.bo.demand.BugBO;
import com.zj.domain.entity.bo.demand.DemandBO;
import com.zj.domain.entity.bo.demand.WorkTaskBO;
import com.zj.domain.entity.enums.BugStatus;
import com.zj.domain.entity.enums.DemandStatus;
import com.zj.domain.entity.enums.RelationType;
import com.zj.domain.entity.enums.WorkTaskStatus;
import com.zj.domain.repository.demand.IBugCommandRepository;
import com.zj.domain.repository.demand.IDemandCommandRepository;
import com.zj.domain.repository.demand.IWorkTaskCommandRepository;
import com.zj.domain.repository.pipeline.ICodeChangeQueryRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 发布结束后的业务域状态推进。
 *
 * 这段规则原先长在 master 的 PublishEndListener 里，跨进程直接改了 demand/bug/work_task 三张表。
 * 现在 master 只宣告流水线结束，需求与缺陷怎么变由业务域自己决定。
 *
 * @author guyuelan
 */
@Slf4j
@Service
public class PublishEndService {

    private final IDemandCommandRepository demandRepository;
    private final IBugCommandRepository bugRepository;
    private final IWorkTaskCommandRepository workTaskRepository;
    private final ICodeChangeQueryRepository codeChangeRepository;

    public PublishEndService(IDemandCommandRepository demandRepository, IBugCommandRepository bugRepository,
        IWorkTaskCommandRepository workTaskRepository, ICodeChangeQueryRepository codeChangeRepository) {
        this.demandRepository = demandRepository;
        this.bugRepository = bugRepository;
        this.workTaskRepository = workTaskRepository;
        this.codeChangeRepository = codeChangeRepository;
    }

    /**
     * 分支发布完成则会自动修改关联的需求、缺陷、工作项状态
     *
     * @param relationMap     relationType -> 关联的业务ID列表，由调用方从代码变更聚合出来
     * @param publishBranches 本次发布包含的分支
     */
    public void finishCodeRelatedResource(Map<Integer, List<String>> relationMap, List<String> publishBranches) {
        if (Objects.isNull(relationMap) || relationMap.isEmpty()) {
            log.info("no relation source to finish");
            return;
        }

        List<String> demandIds = relationMap.get(RelationType.DEMAND.getType());
        if (CollectionUtils.isNotEmpty(demandIds)) {
            batchUpdateDemands(demandIds, publishBranches);
        }

        List<String> bugIds = relationMap.get(RelationType.BUG.getType());
        if (CollectionUtils.isNotEmpty(bugIds)) {
            batchUpdateBugs(bugIds, publishBranches);
        }

        List<String> taskIds = relationMap.get(RelationType.WORK.getType());
        if (CollectionUtils.isNotEmpty(taskIds)) {
            List<String> notCompleteTaskIds = workTaskRepository.getNotCompleteWorkTasks(taskIds).stream()
                    .map(WorkTaskBO::getTaskId).collect(Collectors.toList());
            boolean workTaskUpdate =
                    workTaskRepository.batchUpdateStatus(notCompleteTaskIds, WorkTaskStatus.COMPLETE.getType());
            log.info("batch update work task result ={}", workTaskUpdate);
        }
    }

    private void batchUpdateBugs(List<String> bugIds, List<String> publishBranches) {
        //将缺陷列表转化成变更分支是否全部发布的map
        Map<Boolean, List<String>> partitionMap = bugIds.stream().collect(Collectors.partitioningBy(bugId ->
                isAllBranchPublish(bugId, RelationType.BUG, publishBranches)));

        //将缺陷变更为发布状态
        List<String> completeBugIds = partitionMap.get(true);
        List<String> notPublishBugIds = bugRepository.getNotCompleteBugs(completeBugIds).stream()
                .map(BugBO::getBugId).collect(Collectors.toList());
        boolean bugUpdate = bugRepository.batchUpdateStatus(notPublishBugIds, BugStatus.PUBLISHED.getType());
        log.info("batch update complete bug result ={} id={}", bugUpdate, notPublishBugIds);

        //将未全部发布的缺陷变更为研发中状态
        List<String> notCompleteBugIds = partitionMap.get(false);
        boolean updateStatus = bugRepository.batchUpdateProcessing(notCompleteBugIds);
        log.info("update bug processing result ={} id={}", updateStatus, notCompleteBugIds);
    }

    private void batchUpdateDemands(List<String> demandIds, List<String> branches) {
        //将需求列表转化成变更分支是否全部发布的map
        Map<Boolean, List<String>> partitionMap = demandIds.stream().collect(Collectors.partitioningBy(demandId ->
                isAllBranchPublish(demandId, RelationType.DEMAND, branches)));

        //将需求变更为发布状态
        List<String> completeDemandIds = partitionMap.get(true);
        List<String> notPublishDemandIds = demandRepository.getNotCompleteDemandByIds(completeDemandIds).stream()
                .map(DemandBO::getDemandId).collect(Collectors.toList());
        boolean demandUpdate =
                demandRepository.batchUpdateStatus(notPublishDemandIds, DemandStatus.PUBLISHED.getType());
        log.info("batch update complete demand result ={} id={}", demandUpdate, notPublishDemandIds);

        //将未全部发布的需求变更为研发中状态
        List<String> notCompleteDemandIds = partitionMap.get(false);
        boolean updateStatus = demandRepository.batchUpdateProcessing(notCompleteDemandIds);
        log.info("update demand processing result ={} id={}", updateStatus, notCompleteDemandIds);
    }

    private boolean isAllBranchPublish(String relationId, RelationType relationType, List<String> branches) {
        return codeChangeRepository.getCodeChangeByRelationId(relationId, relationType.getType()).stream()
                .allMatch(codeChange -> branches.contains(codeChange.getChangeBranch()));
    }
}
