package com.zj.domain.entity.po.feature;

import lombok.Data;

@Data
public class FeatureHistory {
    private Long id;

    /**
     * 用户执行历史ID
     */
    private String historyId;

    /**
     * 用例Id
     * */
    private String featureId;

    /**
     * 执行记录Id
     * */
    private String recordId;

    /**
     * 用例名称
     * */
    private String featureName;

    /**
     * 执行状态
     * */
    private Integer executeStatus;

    /**
     * 创建时间
     * */
    private Long createTime;

    /**
     * 修改时间
     */
    private Long updateTime;
}
