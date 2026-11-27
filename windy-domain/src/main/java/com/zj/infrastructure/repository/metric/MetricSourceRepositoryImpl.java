package com.zj.infrastructure.repository.metric;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.metric.MetricSourceBO;
import com.zj.infrastructure.entity.po.metric.MetricSource;
import com.zj.infrastructure.mapper.metric.MetricSourceMapper;
import com.zj.domain.repository.metric.IMetricSourceCommandRepository;
import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MetricSourceRepositoryImpl extends ServiceImpl<MetricSourceMapper, MetricSource> implements IMetricSourceCommandRepository {

    @Override
    public List<MetricSourceBO> loadYesterdaySourceData() {
        DateTime now = DateTime.now();
        DateTime startOfYesterday = now.minusDays(1).withTimeAtStartOfDay();
        DateTime endOfYesterday = now.withTimeAtStartOfDay();
        List<MetricSource> metricSources = list(Wrappers.lambdaQuery(MetricSource.class).ge(MetricSource::getCreateTime, startOfYesterday.getMillis())
                .lt(MetricSource::getCreateTime, endOfYesterday.getMillis()));
        return OrikaUtil.convertList(metricSources, MetricSourceBO.class);
    }
}
