package com.zj.domain.repository.metric;

import com.zj.domain.entity.bo.metric.MetricResultBO;
import java.util.List;
import com.zj.domain.repository.marker.ConfigCommandRepository;


/**
 * @author guyuelan
 */
public interface IMetricResultCommandRepository extends IMetricResultQueryRepository, ConfigCommandRepository {

  boolean batchSaveResult(List<MetricResultBO> metricResultList);

}
