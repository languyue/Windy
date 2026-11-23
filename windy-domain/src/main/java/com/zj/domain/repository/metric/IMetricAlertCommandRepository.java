package com.zj.domain.repository.metric;

import com.zj.domain.repository.marker.ConfigCommandRepository;

/**
 * 告警规则写接口，唯一写 owner 是 windy-console。
 *
 * @author guyuelan
 */
public interface IMetricAlertCommandRepository extends IMetricAlertQueryRepository,
    ConfigCommandRepository {
}
