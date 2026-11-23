package com.zj.metrics.schedule;

import com.zj.common.utils.TraceUtils;
import com.zj.domain.entity.bo.metric.MetricDefinitionBO;
import com.zj.domain.entity.bo.metric.MetricSourceBO;
import com.zj.domain.repository.metric.IMetricDefinitionQueryRepository;
import com.zj.domain.repository.metric.IMetricSourceQueryRepository;
import com.zj.metrics.calculate.MetricParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Component
public class MetricScanSchedule {

    private final IMetricSourceQueryRepository metricSourceRepository;
    private final IMetricDefinitionQueryRepository metricDefinitionRepository;
    private final MetricParser metricParser;
    private final Executor metricPool;

    public MetricScanSchedule(IMetricSourceQueryRepository metricSourceRepository, IMetricDefinitionQueryRepository metricDefinitionRepository,
                              MetricParser metricParser, @Qualifier("metricParserPool") Executor metricPool) {
        this.metricSourceRepository = metricSourceRepository;
        this.metricDefinitionRepository = metricDefinitionRepository;
        this.metricParser = metricParser;
        this.metricPool = metricPool;
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void scanMetrics() {
        TraceUtils.initTrace();
        List<MetricSourceBO> metricSources = metricSourceRepository.loadYesterdaySourceData();
        List<MetricDefinitionBO> metricDefinitions = metricDefinitionRepository.getAllMetricDefinitions();
        metricDefinitions.forEach(metricDefinition -> {
            CompletableFuture.supplyAsync(() -> metricParser.startParse(metricDefinition, metricSources), metricPool)
                    .whenComplete((count, throwable) -> {
                        log.info("handle metric definition={} complete count={}", metricDefinition.getMetricName(), count);
                    }).exceptionally(throwable -> {
                        log.error("handle metric definition={} error: ", metricDefinition.getMetricName(), throwable);
                        return null;
                    });
        });

    }
}
