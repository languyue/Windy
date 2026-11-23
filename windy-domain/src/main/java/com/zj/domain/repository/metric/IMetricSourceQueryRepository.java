package com.zj.domain.repository.metric;

import com.zj.domain.entity.bo.metric.MetricSourceBO;
import java.util.List;


/**
 * @author guyuelan
 */
public interface IMetricSourceQueryRepository {

  List<MetricSourceBO> loadYesterdaySourceData();

}
