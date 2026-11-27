package com.zj.master.dispatch.pipeline.listener;

import com.zj.contract.port.IConsoleInvoker;
import com.zj.contract.event.PipelineFinishedEvent;
import com.zj.domain.entity.bo.pipeline.PipelineBO;
import com.zj.domain.entity.enums.PipelineType;
import com.zj.domain.repository.pipeline.IPipelineQueryRepository;
import com.zj.contract.json.ContractJson;
import com.zj.master.entity.vo.NodeStatusChange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 发布流水线结束后向 console 宣告事件。
 *
 * 需求、缺陷、工作项、代码变更、发布绑定都属于业务域，它们的后续状态变化是 console 的业务规则，
 * master 只负责报告"这条发布流水线成功了"，不再跨进程写别人的表。
 *
 * @author guyuelan
 * @since 2023/6/29
 */
@Slf4j
@Component
public class PublishEndListener implements IPipelineEndListener {

    private final IPipelineQueryRepository pipelineRepository;
    private final IConsoleInvoker consoleInvoker;

    public PublishEndListener(IPipelineQueryRepository pipelineRepository, IConsoleInvoker consoleInvoker) {
        this.pipelineRepository = pipelineRepository;
        this.consoleInvoker = consoleInvoker;
    }

    @Override
    public void handleEnd(NodeStatusChange statusChange) {
        PipelineBO pipeline = pipelineRepository.getPipeline(statusChange.getPipelineId());
        if (Objects.isNull(pipeline)) {
            return;
        }

        log.info("pipeline info = {}", ContractJson.write(pipeline));
        if (!Objects.equals(pipeline.getPipelineType(), PipelineType.PUBLISH.getType())
                || !statusChange.getProcessStatus().isSuccess()) {
            return;
        }

        PipelineFinishedEvent event = new PipelineFinishedEvent();
        event.setPipelineId(pipeline.getPipelineId());
        event.setHistoryId(statusChange.getHistoryId());
        event.setServiceId(pipeline.getServiceId());
        event.setPipelineType(pipeline.getPipelineType());
        event.setProcessStatus(statusChange.getProcessStatus().getType());
        boolean delivered = consoleInvoker.notifyPipelineFinished(event);
        log.info("publish pipeline finished event pipelineId={} delivered={}", pipeline.getPipelineId(), delivered);
    }
}
