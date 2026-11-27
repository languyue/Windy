package com.zj.infrastructure.entity.po.metric;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 指标告警
 */
@Data
@TableName("metric_alert_rule")
public class MetricAlert {

    private Long id;

    /**
     * 告警规则ID
     */
    private String ruleId;

    /**
     * 指标ID
     */
    private String metricId;

    /**
     * 计算条件
     */
    private String condition;

    /**
     * 通知通道
     */
    private String notifyChannel;

    /**
     * 通知模版
     */
    private String notifyTemplate;

    private Long createTime;

    private Long updateTime;
}
