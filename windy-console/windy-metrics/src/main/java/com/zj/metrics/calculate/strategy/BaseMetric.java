package com.zj.metrics.calculate.strategy;

import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.domain.entity.bo.metric.MetricResultBO;
import com.zj.domain.entity.enums.DimensionType;
import com.zj.domain.repository.metric.IMetricResultCommandRepository;
import com.zj.metrics.calculate.IMetric;

import java.util.List;

public abstract class BaseMetric implements IMetric {

    private final UniqueIdService uniqueIdService;
    private final IMetricResultCommandRepository metricResultRepository;

    protected BaseMetric(UniqueIdService uniqueIdService, IMetricResultCommandRepository metricResultRepository) {
        this.uniqueIdService = uniqueIdService;
        this.metricResultRepository = metricResultRepository;
    }

    public MetricResultBO createMetricResult(String resultName, String metricId, String tag, Double value) {
        return createMetricResult(resultName,metricId,tag,value, null);
    }

    public MetricResultBO createMetricResult(String resultName, String metricId, String tag, Double value, String resultType) {
        MetricResultBO metricResultBO = new MetricResultBO();
        metricResultBO.setMetricId(uniqueIdService.getUniqueId());
        metricResultBO.setDimension(DimensionType.STATISTICS.getType());
        metricResultBO.setMetricId(metricId);
        metricResultBO.setValue(value);
        metricResultBO.setCreateTime(System.currentTimeMillis());
        metricResultBO.setResultName(resultName);
        metricResultBO.setTag(tag);
        metricResultBO.setResultType(resultType);
        return metricResultBO;
    }

    public boolean batchSaveMetric(List<MetricResultBO> metricResultList) {
        return metricResultRepository.batchSaveResult(metricResultList);
    }

}
