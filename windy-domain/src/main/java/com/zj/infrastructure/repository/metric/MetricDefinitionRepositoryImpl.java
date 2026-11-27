package com.zj.infrastructure.repository.metric;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.metric.MetricDefinitionBO;
import com.zj.infrastructure.entity.po.metric.MetricDefinition;
import com.zj.infrastructure.mapper.metric.MetricDefinitionMapper;
import com.zj.domain.repository.metric.IMetricDefinitionCommandRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MetricDefinitionRepositoryImpl extends ServiceImpl<MetricDefinitionMapper, MetricDefinition> implements IMetricDefinitionCommandRepository {

    @Override
    public List<MetricDefinitionBO> getAllMetricDefinitions() {
        List<MetricDefinition> metricDefinitions = list();
        return OrikaUtil.convertList(metricDefinitions, MetricDefinitionBO.class);
    }
}
