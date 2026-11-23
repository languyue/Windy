package com.zj.domain.repository.metric;

import com.zj.domain.entity.bo.metric.MetricDefinitionBO;
import java.util.List;


/**
 * @author guyuelan
 */
public interface IMetricDefinitionQueryRepository {

  List<MetricDefinitionBO> getAllMetricDefinitions();

}
