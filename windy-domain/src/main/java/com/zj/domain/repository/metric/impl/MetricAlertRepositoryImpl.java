package com.zj.domain.repository.metric.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.domain.entity.po.metric.MetricAlert;
import com.zj.domain.mapper.metric.MetricAlertMapper;
import com.zj.domain.repository.metric.IMetricAlertCommandRepository;
import org.springframework.stereotype.Repository;

@Repository
public class MetricAlertRepositoryImpl extends ServiceImpl<MetricAlertMapper, MetricAlert> implements IMetricAlertCommandRepository {
}
