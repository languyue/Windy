package com.zj.metrics.service;

import com.zj.domain.entity.bo.demand.DemandBO;
import com.zj.domain.entity.bo.metric.MetricResultBO;
import com.zj.domain.entity.enums.DemandStatus;
import com.zj.domain.repository.demand.IDemandQueryRepository;
import com.zj.domain.repository.metric.IMetricResultQueryRepository;
import com.zj.metrics.calculate.strategy.MetricNameType;
import com.zj.metrics.entity.*;
import com.zj.metrics.utils.MetricUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MetricsService {

    private final IDemandQueryRepository demandRepository;
    private final IMetricResultQueryRepository metricResultRepository;

    public MetricsService(IDemandQueryRepository demandRepository, IMetricResultQueryRepository metricResultRepository) {
        this.demandRepository = demandRepository;
        this.metricResultRepository = metricResultRepository;
    }

    public List<DemandStatisticsDto> getDemandStatistics() {
        List<DemandBO> allDemands = demandRepository.getAllDemands();
        Map<String, List<DemandBO>> splitDemands = allDemands.stream()
                .filter(Objects::nonNull).filter(demandBO -> StringUtils.isNotBlank(demandBO.getCustomerValue()))
                .collect(Collectors.groupingBy(DemandBO::getCustomerValue));

        return splitDemands.keySet().stream().map(customerValue -> {
            DemandStatisticsDto countStatistics = new DemandStatisticsDto();
            List<DemandBO> demandBOS = splitDemands.get(customerValue);
            countStatistics.setDataValue(demandBOS.size());
            countStatistics.setDemandType(customerValue);
            countStatistics.setDataType(MetricNameType.DEMAND_CATEGORY_COUNT.getMetricName());

            DemandStatisticsDto workloadStatistics = new DemandStatisticsDto();
            double count = demandBOS.stream().mapToInt(DemandBO::getWorkload).sum();
            workloadStatistics.setDataValue((int) count);
            workloadStatistics.setDemandType(customerValue);
            workloadStatistics.setDataType(MetricNameType.DEMAND_CATEGORY_WORKLOAD.getMetricName());
            return Arrays.asList(countStatistics, workloadStatistics);
        }).flatMap(Collection::stream).collect(Collectors.toList());
    }

    public List<DemandStatusStatisticsDto> getDemandStatusStatistics() {
        List<DemandBO> allDemands = demandRepository.getAllDemands();
        Map<Integer, List<DemandBO>> statusMap = allDemands.stream().collect(Collectors.groupingBy(DemandBO::getStatus));
        return statusMap.keySet().stream().map(status -> {
            DemandStatus demandStatus = DemandStatus.convertType(status);
            if (Objects.isNull(demandStatus)) {
                log.info("can not find demand status={}", status);
                return null;
            }
            List<DemandBO> demands = statusMap.get(status);
            double percent =Optional.of(demands).filter(CollectionUtils::isNotEmpty)
                    .map(list -> MetricUtils.scaleTo1Decimal((list.size() * 100d) / allDemands.size())).orElse(0d);

            DemandStatusStatisticsDto statusStatistics = new DemandStatusStatisticsDto();
            statusStatistics.setStatusName(demandStatus.getDesc());
            statusStatistics.setPercent(percent);
            return statusStatistics;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public HealthyMetricDto getSystemHealthy() {
        MetricResultBO metricResultBO = metricResultRepository.getLatestMetricByType("system_health",
                MetricNameType.SYSTEM_HEALTH.getMetricName());
        if (Objects.isNull(metricResultBO)) {
            return new HealthyMetricDto(0);
        }
        return new HealthyMetricDto(metricResultBO.getValue().intValue());
    }

    public List<DemandDelayWorkloadDto> getDemandWorkload(Long startTime) {
        List<MetricResultBO> metricResultList = metricResultRepository.getDemandDelayWorkLoadByTag("demand_delay",
                startTime);
        return metricResultList.stream().map(metricResultBO -> {
            DemandDelayWorkloadDto delayWorkloadDto = new DemandDelayWorkloadDto();
            delayWorkloadDto.setMetricName(metricResultBO.getResultName());
            delayWorkloadDto.setValue(metricResultBO.getValue());
            delayWorkloadDto.setTime(metricResultBO.getCreateTime());
            return delayWorkloadDto;
        }).collect(Collectors.toList());
    }

    public List<BugStatusStatisticsDto> getBugStatusStatistics(Long startTime) {
        List<MetricResultBO> metricResultList = metricResultRepository.getDemandDelayWorkLoadByTag("bug_statistics", startTime);
        return metricResultList.stream().map(metricResultBO -> {
            BugStatusStatisticsDto statusStatisticsDto = new BugStatusStatisticsDto();
            statusStatisticsDto.setStatusName(metricResultBO.getResultName());
            statusStatisticsDto.setValue(metricResultBO.getValue());
            statusStatisticsDto.setTime(metricResultBO.getCreateTime());
            return statusStatisticsDto;
        }).collect(Collectors.toList());
    }


    public List<HealthyStatisticsDto> getSystemHealthyStatistics(Long startTime) {
        List<MetricResultBO> metricResultList = metricResultRepository.getDemandDelayWorkLoadByTag("system_health", startTime);
        return metricResultList.stream().map(metricResultBO -> {
            HealthyStatisticsDto healthyStatisticsDto = new HealthyStatisticsDto();
            healthyStatisticsDto.setTypeName(metricResultBO.getResultName());
            healthyStatisticsDto.setValue(metricResultBO.getValue());
            healthyStatisticsDto.setTime(metricResultBO.getCreateTime());
            return healthyStatisticsDto;
        }).collect(Collectors.toList());
    }
}
