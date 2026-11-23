package com.zj.metrics.rest;

import com.zj.contract.rpc.ResponseMeta;
import com.zj.contract.exception.ErrorCode;
import com.zj.metrics.entity.*;
import com.zj.metrics.service.MetricsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/devops/metric")
public class MetricsRest {

    private final MetricsService metricsService;

    public MetricsRest(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/demand/statistics")
    public ResponseMeta<List<DemandStatisticsDto>> getDemandStatistics() {
        return new ResponseMeta<>(ErrorCode.SUCCESS, metricsService.getDemandStatistics());
    }

    @GetMapping("/demand/status/statistics")
    public ResponseMeta<List<DemandStatusStatisticsDto>> getDemandStatusStatistics() {
        return new ResponseMeta<>(ErrorCode.SUCCESS, metricsService.getDemandStatusStatistics());
    }

    @GetMapping("/system/healthy")
    public ResponseMeta<HealthyMetricDto> getSystemHealthy() {
        return new ResponseMeta<>(ErrorCode.SUCCESS, metricsService.getSystemHealthy());
    }

    @GetMapping("/demand/workload")
    public ResponseMeta<List<DemandDelayWorkloadDto>> getDemandWorkload(@RequestParam("startTime") Long startTime) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, metricsService.getDemandWorkload(startTime));
    }

    @GetMapping("/bug/statistics")
    public ResponseMeta<List<BugStatusStatisticsDto>> getBugStatusStatistics(@RequestParam("startTime") Long startTime) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, metricsService.getBugStatusStatistics(startTime));
    }

    @GetMapping("/healthy/statistics")
    public ResponseMeta<List<HealthyStatisticsDto>> getSystemHealthyStatistics(@RequestParam("startTime") Long startTime) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, metricsService.getSystemHealthyStatistics(startTime));
    }
}
