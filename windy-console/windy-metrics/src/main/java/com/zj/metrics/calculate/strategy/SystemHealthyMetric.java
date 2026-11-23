package com.zj.metrics.calculate.strategy;

import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.domain.entity.bo.demand.BugBO;
import com.zj.domain.entity.bo.demand.DemandBO;
import com.zj.domain.entity.bo.metric.MetricDefinitionBO;
import com.zj.domain.entity.bo.metric.MetricResultBO;
import com.zj.domain.entity.bo.metric.MetricSourceBO;
import com.zj.domain.entity.enums.BugStatus;
import com.zj.domain.entity.enums.DemandStatus;
import com.zj.domain.repository.demand.IBugQueryRepository;
import com.zj.domain.repository.demand.IDemandQueryRepository;
import com.zj.domain.repository.metric.IMetricResultCommandRepository;
import com.zj.metrics.utils.MetricUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SystemHealthyMetric extends BaseMetric {

    private final IDemandQueryRepository demandRepository;
    private final IBugQueryRepository bugRepository;

    protected SystemHealthyMetric(UniqueIdService uniqueIdService, IMetricResultCommandRepository metricResultRepository,
                                  IDemandQueryRepository demandRepository, IBugQueryRepository bugRepository) {
        super(uniqueIdService, metricResultRepository);
        this.demandRepository = demandRepository;
        this.bugRepository = bugRepository;
    }

    @Override
    public String getMetricType() {
        return "system_health";
    }

    @Override
    public boolean matchMetric(String category, String calcType) {
        return Objects.equals(category, "system") && Objects.equals(calcType, "health");
    }

    @Override
    public Integer calculateMetric(MetricDefinitionBO metricDefinition, List<MetricSourceBO> metricSources) {
        log.info("start calculate system health metric");
        List<DemandBO> allDemands = demandRepository.getAllDemands();
        if (CollectionUtils.isEmpty(allDemands)) {
            log.info("demand list is empty, not calculate");
            return 0;
        }
        //计算需求完成率
        List<DemandBO> completeDemands = allDemands.stream().filter(demandBO -> Objects.equals(demandBO.getStatus(),
                DemandStatus.PUBLISHED.getType())).collect(Collectors.toList());
        double demandCompletePercent = MetricUtils.scaleTo1Decimal(completeDemands.size() * 100d / allDemands.size());
        MetricResultBO demandPercentMetric = createMetricResult(MetricNameType.DEMAND_COMPLETE_PERCENT.getMetricName(),
                metricDefinition.getMetricId(), getMetricType(), demandCompletePercent);

        //计算缺陷修复率
        List<BugBO> allBugs = bugRepository.getAllBugs();
        List<BugBO> completeBugs = allBugs.stream()
                .filter(bugBO -> Objects.equals(bugBO.getStatus(), BugStatus.PUBLISHED.getType()))
                .collect(Collectors.toList());
        double bugCompletePercent = MetricUtils.scaleTo1Decimal(completeBugs.size() * 100d / allBugs.size());
        MetricResultBO bugPercentMetric = createMetricResult(MetricNameType.BUG_COMPLETE_PERCENT.getMetricName(),
                metricDefinition.getMetricId(), getMetricType(), bugCompletePercent);

        //计算需求逾期率
        List<DemandBO> overdueDemands = allDemands.stream().filter(demandBO ->
                !Objects.equals(demandBO.getStatus(), DemandStatus.PUBLISHED.getType())
                && System.currentTimeMillis() > demandBO.getExpectTime())
                .collect(Collectors.toList());
        double overduePercent = MetricUtils.scaleTo1Decimal(overdueDemands.size() * 100d / allDemands.size());
        MetricResultBO overduePercentMetric = createMetricResult(MetricNameType.DEMAND_OVERDUE_PERCENT.getMetricName(),
                metricDefinition.getMetricId(), getMetricType(), overduePercent);

        double healthPercent = calculateHealthy(demandCompletePercent, bugCompletePercent, overduePercent);
        MetricResultBO healthMetric = createMetricResult(MetricNameType.SYSTEM_HEALTH.getMetricName(),
                metricDefinition.getMetricId(), getMetricType(), healthPercent);

        List<MetricResultBO> list = Arrays.asList(demandPercentMetric, bugPercentMetric, overduePercentMetric, healthMetric);
        batchSaveMetric(list);
        log.info("calculate system health metric end percent={}", healthPercent);
        return list.size();
    }

    /**
     * 计算系统健康度，计算规则如下：
     * 健康度总分=(0.4×需求完成率)+(0.3×缺陷解决率)+(0.3×(100−任务逾期率))
     * 注：任务逾期率是负向指标，需反向计算（如逾期率20%，则正向得分为80%）。
     * @param demandCompletePercent 需求完成百分比
     * @param bugCompletePercent 缺陷完成百分比
     * @param overduePercent 需求逾期百分比
     * @return 健康度总分
     */
    private double calculateHealthy(double demandCompletePercent, double bugCompletePercent, double overduePercent) {
        return 0.4 * demandCompletePercent + 0.3 * bugCompletePercent + (0.3 * (100 - overduePercent));
    }
}
