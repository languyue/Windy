package com.zj.infrastructure.entity.po.metric;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 指标定义
 */
@Data
@TableName("metric_definition")
public class MetricDefinition {

    private Long id;

    /**
     * 指标ID
     */
    private String metricId;

    /**
     * 指标名称
     */
    private String metricName;

    /**
     * 指标类型
     */
    private String category;

    /**
     * 计算方式
     */
    private String calcType;

    private Long createTime;

    private Long updateTime;
}
