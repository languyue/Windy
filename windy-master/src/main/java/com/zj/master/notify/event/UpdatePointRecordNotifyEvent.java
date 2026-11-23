package com.zj.master.notify.event;

import com.zj.contract.event.ResultEvent;
import com.zj.contract.enums.NotifyType;
import com.zj.contract.enums.ProcessStatus;
import com.zj.common.enums.TemplateType;
import com.zj.domain.entity.bo.feature.ExecuteRecordBO;
import com.zj.domain.entity.bo.feature.FeatureHistoryBO;
import com.zj.domain.repository.feature.IExecuteRecordCommandRepository;
import com.zj.domain.repository.feature.IFeatureHistoryCommandRepository;
import com.zj.master.notify.INotifyEvent;
import com.zj.master.notify.NotifyPayloadMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Slf4j
@Component
public class UpdatePointRecordNotifyEvent implements INotifyEvent {

    private final IExecuteRecordCommandRepository executeRecordRepository;
    private final IFeatureHistoryCommandRepository historyRepository;

    public UpdatePointRecordNotifyEvent(IExecuteRecordCommandRepository executeRecordRepository,
                                        IFeatureHistoryCommandRepository historyRepository) {
        this.executeRecordRepository = executeRecordRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    public NotifyType type() {
        return NotifyType.UPDATE_EXECUTE_POINT_RECORD;
    }

    @Override
    public boolean handle(ResultEvent resultEvent) {
        log.info("receive execute record update event id = {} event={}", resultEvent.getExecuteId(),
                resultEvent.getParams());
        ExecuteRecordBO executeRecordBO = NotifyPayloadMapper.toExecuteRecordBO(resultEvent);
        updateFeatureHistory(executeRecordBO);
        return executeRecordRepository.updateStatusAndResult(executeRecordBO);
    }

    private void updateFeatureHistory(ExecuteRecordBO executeRecordBO) {
        if (!Objects.equals(TemplateType.THREAD.getType(), executeRecordBO.getExecuteType())) {
            return;
        }

        //如果用例状态已经是失败状态则不需要处理
        String historyId = executeRecordBO.getHistoryId();
        FeatureHistoryBO featureHistory = historyRepository.getFeatureHistory(historyId);
        if (ProcessStatus.exchange(featureHistory.getExecuteStatus()).isFailStatus()) {
            return;
        }

        // 当前执行点执行的结果为失败状态时才需更新用例状态
        if (ProcessStatus.exchange(executeRecordBO.getStatus()).isFailStatus() ||
                Objects.equals(featureHistory.getExecuteStatus(), ProcessStatus.RUNNING.getType())) {
            featureHistory.setExecuteStatus(executeRecordBO.getStatus());
            boolean update = historyRepository.updateStatus(executeRecordBO.getHistoryId(),
                    executeRecordBO.getStatus());
            log.info("async execute record update feature history status = {} recordId={} historyId={} result={}",
                    executeRecordBO.getStatus(), executeRecordBO.getExecuteRecordId(), historyId, update);
        }
    }
}
