package com.zj.feature.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.common.adapter.auth.IAuthService;
import com.zj.contract.enums.LogType;
import com.zj.contract.enums.ProcessStatus;
import com.zj.contract.dispatch.DispatchTaskModel;
import com.zj.contract.feature.exec.BatchFeatureVo;
import com.zj.contract.rpc.PageSize;
import com.zj.common.adapter.invoker.IMasterCommand;
import com.zj.common.adapter.invoker.IMasterApi;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.common.entity.command.TaskRecordCommand;
import com.zj.common.exception.ApiException;
import com.zj.contract.exception.ErrorCode;
import com.zj.domain.entity.bo.feature.FeatureInfoBO;
import com.zj.domain.entity.bo.feature.TestCaseBO;
import com.zj.domain.entity.enums.TaskRecordType;
import com.zj.domain.repository.feature.IFeatureQueryRepository;
import com.zj.domain.repository.feature.ITestCaseConfigCommandRepository;
import com.zj.domain.repository.feature.ITestCaseCommandRepository;
import com.zj.feature.entity.BatchExecuteFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author guyuelan
 * @since 2022/12/12
 */
@Slf4j
@Service
public class TestCaseService {
    private final UniqueIdService uniqueIdService;
    private final ITestCaseCommandRepository testCaseRepository;
    private final ITestCaseConfigCommandRepository testCaseConfigRepository;
    private final IFeatureQueryRepository featureRepository;
    private final IMasterCommand masterCommand;
    private final IAuthService authService;
    private final IMasterApi masterInvoker;

    public TestCaseService(UniqueIdService uniqueIdService, ITestCaseCommandRepository testCaseRepository,
                           ITestCaseConfigCommandRepository testCaseConfigRepository, IFeatureQueryRepository featureRepository, 
                           IMasterCommand masterCommand,IAuthService authService, IMasterApi masterInvoker) {
        this.uniqueIdService = uniqueIdService;
        this.testCaseRepository = testCaseRepository;
        this.testCaseConfigRepository = testCaseConfigRepository;
        this.featureRepository = featureRepository;
        this.masterCommand = masterCommand;
        this.authService = authService;
        this.masterInvoker = masterInvoker;
    }

    public PageSize<TestCaseBO> getE2ECases(Integer page, Integer pageSize) {
        IPage<TestCaseBO> pageObj = testCaseRepository.getE2ECasesPage(page, pageSize);
        return convertPageSize(pageObj);
    }

    public PageSize<TestCaseBO> convertPageSize(IPage<TestCaseBO> page) {
        List<TestCaseBO> records = page.getRecords();
        PageSize<TestCaseBO> dtoPageSize = new PageSize<>();
        if (CollectionUtils.isEmpty(records)) {
            dtoPageSize.setTotal(0);
            return dtoPageSize;
        }
        long total = page.getTotal();
        dtoPageSize.setTotal(total);
        dtoPageSize.setData(records);
        return dtoPageSize;
    }

    public PageSize<TestCaseBO> getTestCaseList(String serviceId, Integer page, Integer pageSize) {
        IPage<TestCaseBO> pageObj = testCaseRepository.getCasePage(serviceId, page, pageSize);
        return convertPageSize(pageObj);
    }

    public String createTestCase(TestCaseBO testCaseBO) {
        String testCaseId = uniqueIdService.getUniqueId();
        testCaseBO.setUserId(authService.getCurrentUserId());
        testCaseBO.setTestCaseId(testCaseId);
        boolean saveCase = testCaseRepository.saveCase(testCaseBO);
        return saveCase ? testCaseId : "";
    }

    public Boolean updateTestCase(TestCaseBO testCaseBO) {
        long dateNow = System.currentTimeMillis();
        testCaseBO.setUpdateTime(dateNow);
        return testCaseRepository.updateCase(testCaseBO);
    }

    public TestCaseBO getTestCase(String caseId) {
        return testCaseRepository.getTestCaseById(caseId);
    }

    public Boolean deleteTestCase(String caseId) {
        List<FeatureInfoBO> featureList = featureRepository.queryFeatureList(caseId);
        if (CollectionUtils.isNotEmpty(featureList)) {
            log.info("there are features in the case , can not delete caseId={}", caseId);
            throw new ApiException(ErrorCode.CASE_EXIST_FEATURE);
        }
        boolean result = testCaseConfigRepository.deleteByCaseId(caseId);
        log.info("delete case config result = {}", result);
        return testCaseRepository.deleteTestCase(caseId);
    }

    public List<TestCaseBO> getE2ECases() {
        return testCaseRepository.getE2ECases();
    }

    /**
     * 临时批量执行也需要一条 task_record 作为执行容器，但它的写权在 master，
     * 这里只下发命令拿回 recordId 作为本次执行的触发标识。
     */
    public boolean executeFeature(String caseId, BatchExecuteFeature batchExecute) {
        String taskName = "批量执行 " + authService.getUserDetail().getNickName();
        String recordId = masterCommand.createTaskRecord(buildTempTaskRecord(caseId, taskName, batchExecute));
        if (StringUtils.isBlank(recordId)) {
            log.info("create record error, can not execute batch features caseId={}", caseId);
            return false;
        }

        DispatchTaskModel dispatchTaskModel = new DispatchTaskModel();
        dispatchTaskModel.setType(LogType.FEATURE.getType());
        dispatchTaskModel.setSourceIds(batchExecute.getFeatureIds());
        dispatchTaskModel.setSourceName(taskName);
        dispatchTaskModel.setTriggerIds(Collections.singletonList(recordId));
        String executeRecordId = masterInvoker.runFeatureTask(dispatchTaskModel);
        return Objects.nonNull(executeRecordId);
    }

    private TaskRecordCommand buildTempTaskRecord(String caseId, String taskName, BatchExecuteFeature batchExecute) {
        TaskRecordCommand taskRecord = new TaskRecordCommand();
        taskRecord.setTriggerId(caseId);
        taskRecord.setType(TaskRecordType.TEMP_TASK.getType());
        taskRecord.setTaskConfig(buildTaskConfig(batchExecute));
        taskRecord.setTestCaseId(caseId);
        taskRecord.setStatus(ProcessStatus.RUNNING.getType());
        taskRecord.setTaskName(taskName);
        taskRecord.setUserId(authService.getCurrentUserId());
        return taskRecord;
    }

    private BatchFeatureVo buildTaskConfig(BatchExecuteFeature batchExecute) {
        BatchFeatureVo taskConfig = new BatchFeatureVo();
        taskConfig.setFeatureIds(batchExecute.getFeatureIds());
        return taskConfig;
    }
}
