package com.zj.domain.repository.metric;

import com.zj.domain.entity.bo.metric.MetricResultBO;
import java.util.List;


/**
 * @author guyuelan
 */
public interface IMetricResultQueryRepository {

  MetricResultBO getLatestMetricByType(String tag, String resultName);

  List<MetricResultBO> getDemandDelayWorkLoadByTag(String demandDelay, long startTime);

}
