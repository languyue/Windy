package com.zj.infrastructure.repository.metric;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.infrastructure.entity.po.metric.MetricAlert;
import com.zj.infrastructure.mapper.metric.MetricAlertMapper;
import com.zj.domain.repository.metric.IMetricAlertCommandRepository;
import org.springframework.stereotype.Repository;

@Repository
public class MetricAlertRepositoryImpl extends ServiceImpl<MetricAlertMapper, MetricAlert> implements IMetricAlertCommandRepository {
}
