package com.zj.feature.service;

import com.zj.common.adapter.auth.IAuthService;
import com.zj.contract.port.IMasterCommand;
import com.zj.contract.rpc.PageSize;
import com.zj.common.enums.FeatureStatus;
import com.zj.domain.entity.bo.feature.FeatureHistoryBO;
import com.zj.domain.entity.bo.feature.FeatureInfoBO;
import com.zj.domain.entity.bo.feature.TaskRecordBO;
import com.zj.domain.repository.feature.ITaskRecordQueryRepository;
import com.zj.feature.entity.HistoryNodeDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaskRecordService {

    private final FeatureHistoryService featureHistoryService;
    private final FeatureService featureService;
    private final ITaskRecordQueryRepository taskRecordRepository;
    private final IMasterCommand masterCommand;
    private final IAuthService authService;

    public TaskRecordService(FeatureHistoryService featureHistoryService,
                             FeatureService featureService, ITaskRecordQueryRepository taskRecordRepository,
                             IMasterCommand masterCommand, IAuthService authService) {
        this.featureHistoryService = featureHistoryService;
        this.featureService = featureService;
        this.taskRecordRepository = taskRecordRepository;
        this.masterCommand = masterCommand;
        this.authService = authService;
    }

    public PageSize<TaskRecordBO> getTaskRecordPage(Integer pageNum, Integer size) {
        String currentUserId = authService.getCurrentUserId();
        return taskRecordRepository.getTaskRecordPage(pageNum, size, currentUserId);
    }

    /**
     * task_record 与 feature_history 的写权在 master，这里只下发删除命令
     */
    public boolean deleteTaskRecord(String recordId) {
        TaskRecordBO taskRecord = taskRecordRepository.getTaskRecord(recordId);
        if (Objects.isNull(taskRecord)) {
            return true;
        }
        return masterCommand.deleteTaskRecord(recordId);
    }

    public List<FeatureHistoryBO> getTaskFeatureHistories(String recordId) {
        return featureHistoryService.getHistories(recordId);
    }

    public List<HistoryNodeDto> getTaskFeatureHistoryTree(String recordId) {
        List<FeatureHistoryBO> histories = featureHistoryService.getHistories(recordId);
        if (CollectionUtils.isEmpty(histories)) {
            return Collections.emptyList();
        }

        Map<String, FeatureHistoryBO> historyMap = histories.stream()
                .collect(Collectors.toMap(FeatureHistoryBO::getFeatureId, history -> history));
        TaskRecordBO taskRecord = getTaskRecord(recordId);
        String testCaseId = taskRecord.getTestCaseId();
        List<FeatureInfoBO> featureInfos = featureService.queryFeatureList(testCaseId);
        List<HistoryNodeDto> historyNodes =
                featureInfos.stream().sorted(Comparator.comparing(FeatureInfoBO::getSortOrder))
                .map(feature -> {
                    HistoryNodeDto historyNodeDto = new HistoryNodeDto();
                    historyNodeDto.setParentId(feature.getParentId());
                    historyNodeDto.setRecordId(recordId);
                    historyNodeDto.setSkip(Objects.equals(feature.getStatus(), FeatureStatus.DISABLE.getType()));
                    historyNodeDto.setFeatureId(feature.getFeatureId());
                    historyNodeDto.setFeatureName(feature.getFeatureName());
                    FeatureHistoryBO featureHistory = historyMap.get(feature.getFeatureId());
                    if (Objects.nonNull(featureHistory)) {
                        historyNodeDto.setHistoryId(featureHistory.getHistoryId());
                        historyNodeDto.setExecuteStatus(featureHistory.getExecuteStatus());
                    }
                    return historyNodeDto;
                }).collect(Collectors.toList());

        HistoryNodeDto root = new HistoryNodeDto();
        convertTree(historyNodes, root);
        return root.getChildren();
    }

    private void convertTree(List<HistoryNodeDto> featureList, HistoryNodeDto parent) {
        if (CollectionUtils.isEmpty(featureList)) {
            return;
        }

        List<HistoryNodeDto> list = featureList.stream()
                .filter(feature -> Objects.equals(feature.getParentId(), parent.getFeatureId()))
                .collect(Collectors.toList());
        parent.setChildren(list);

        featureList.removeIf(feature -> Objects.equals(feature.getParentId(), parent.getFeatureId()));
        list.forEach(node -> convertTree(featureList, node));
    }

    public TaskRecordBO getTaskRecord(String recordId) {
        return taskRecordRepository.getTaskRecord(recordId);
    }

    public List<TaskRecordBO> getTaskRecordsByTaskId(String taskId) {
        return taskRecordRepository.getTaskRecords(taskId);
    }

    public Boolean stopTaskRecord(String recordId) {
        //todo 真正的停止应该是让 master 中断正在执行的 dispatch 任务，现在沿用原有的删除记录语义
        return masterCommand.deleteTaskRecord(recordId);
    }

    public TaskRecordBO getTaskRecordByTrigger(String triggerId) {
        return taskRecordRepository.getTaskRecordByTrigger(triggerId);
    }

    public PageSize<TaskRecordBO> getTriggerTaskRecords(String triggerId, Integer page, Integer size) {
        return taskRecordRepository.getTriggerTaskRecords(triggerId, page, size);
    }
}
