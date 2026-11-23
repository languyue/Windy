package com.zj.domain.repository.metric.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.metric.MetricResultBO;
import com.zj.domain.entity.po.metric.MetricResult;
import com.zj.domain.mapper.metric.MetricResultMapper;
import com.zj.domain.repository.metric.IMetricResultCommandRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MetricResultRepositoryImpl extends ServiceImpl<MetricResultMapper, MetricResult> implements IMetricResultCommandRepository {

    @Override
    public boolean batchSaveResult(List<MetricResultBO> metricResultList) {
        List<MetricResult> metricResults = OrikaUtil.convertList(metricResultList, MetricResult.class);
        return saveBatch(metricResults);
    }

    @Override
    public MetricResultBO getLatestMetricByType(String tag, String resultName) {
        MetricResult metricResult = getOne(Wrappers.lambdaQuery(MetricResult.class).eq(MetricResult::getTag, tag)
                .eq(MetricResult::getResultName, resultName).orderByDesc(MetricResult::getCreateTime)
                .last("limit 1"));
        return OrikaUtil.convert(metricResult, MetricResultBO.class);
    }

    @Override
    public List<MetricResultBO> getDemandDelayWorkLoadByTag(String tag, long startTime) {
        List<MetricResult> metricResults = list(Wrappers.lambdaQuery(MetricResult.class).eq(MetricResult::getTag, tag).ge(MetricResult::getCreateTime, startTime));
        return OrikaUtil.convertList(metricResults, MetricResultBO.class);
    }
}
