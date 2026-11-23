package com.zj.master.dispatch.task;

import com.zj.common.enums.FeatureStatus;
import com.zj.contract.enums.ProcessStatus;
import com.zj.domain.entity.bo.feature.FeatureHistoryBO;
import com.zj.domain.entity.bo.feature.FeatureInfoBO;
import com.zj.domain.entity.bo.feature.TaskRecordBO;
import com.zj.domain.repository.feature.IFeatureHistoryQueryRepository;
import com.zj.domain.repository.feature.IFeatureQueryRepository;
import com.zj.domain.repository.feature.ITaskRecordCommandRepository;
import com.zj.domain.repository.log.IDispatchLogCommandRepository;
import com.zj.contract.feature.exec.BatchFeatureVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
@Slf4j
@Component
public class TaskEndProcessor {

    private final ITaskRecordCommandRepository taskRecordRepository;
    private final IFeatureHistoryQueryRepository featureHistoryRepository;
    private final IFeatureQueryRepository featureRepository;
    private final IDispatchLogCommandRepository taskLogRepository;

    public TaskEndProcessor(ITaskRecordCommandRepository taskRecordRepository,
                            IFeatureHistoryQueryRepository featureHistoryRepository, IFeatureQueryRepository featureRepository,
                            IDispatchLogCommandRepository taskLogRepository) {
        this.taskRecordRepository = taskRecordRepository;
        this.featureHistoryRepository = featureHistoryRepository;
        this.featureRepository = featureRepository;
        this.taskLogRepository = taskLogRepository;
    }

    public boolean process(String taskRecordId, String logId) {
        TaskRecordBO taskRecord = taskRecordRepository.getTaskRecord(taskRecordId);
        if (Objects.isNull(taskRecord)) {
            //如果找不到任务记录，可能是任务删除了或者是Id为临时记录Id，无任何业务含义
            log.info("can not find record={} maybe is a temp recordId", taskRecordId);
            return false;
        }

        //1 找到任务记录关联的所有用例
        List<String> featureIds =
                featureRepository.queryNotContainFolder(taskRecord.getTestCaseId()).stream()
                        //过滤disable的用例
                        .filter(feature -> Objects.equals(feature.getStatus(), FeatureStatus.NORMAL.getType()))
                        .map(FeatureInfoBO::getFeatureId)
                        .collect(Collectors.toList());
        if (Objects.equals(taskRecord.getTestCaseId(), taskRecord.getTriggerId())) {
            //如果测试集ID与触发源Id一致证明是临时批量执行任务，需要根据任务选择的用例个数来判断最终状态
            BatchFeatureVo batchFeatureVo = taskRecord.getExecuteConfig();
            featureIds = batchFeatureVo.getFeatureIds();
        }

        //2 找到任务关联所有用例的执行记录
        List<FeatureHistoryBO> featureHistories = featureHistoryRepository.getHistoriesByTaskRecordId(taskRecordId);
        long successCount = featureHistories.stream().filter(featureHistoryBO ->
                Objects.equals(featureHistoryBO.getExecuteStatus(), ProcessStatus.SUCCESS.getType())).count();
        long percent = successCount * 100 / featureIds.size();
        //3 如果所有用例都有执行记录，那么任务执行完成
        if (Objects.equals(featureHistories.size(), featureIds.size())) {
            ProcessStatus processStatus =
                    featureHistories.stream().filter(history -> ProcessStatus.exchange(history.getExecuteStatus())
                    .isFailStatus()).findAny().map(f -> ProcessStatus.FAIL).orElse(ProcessStatus.SUCCESS);
            boolean updateStatus = taskRecordRepository.updateRecordStatusAndPercent(taskRecordId, processStatus.getType(), (int) percent);
            boolean updateLogStatus = taskLogRepository.updateLogStatus(logId, processStatus.getType());
            log.info("task end update recordId={} status={} percent={} logId={} updateStatus={} updateLogStatus={}",
                    taskRecordId, processStatus.getType(), percent, logId, updateStatus, updateLogStatus);
            return true;
        }else{
            boolean updateStatus = taskRecordRepository.updateRecordStatusAndPercent(taskRecordId,null, (int) percent);
            log.info("task percent update recordId={} percent={} updateStatus={}", taskRecordId, percent, updateStatus);
        }
        return false;
    }
}
