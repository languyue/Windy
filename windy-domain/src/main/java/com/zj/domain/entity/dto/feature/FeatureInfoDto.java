package com.zj.domain.entity.dto.feature;

import lombok.Data;

@Data
public class FeatureInfoDto {

    private Long id;

    /**
     * 用例关联的测试集Id
     */
    private String testCaseId;

    /**
     * 用例Id
     */
    private String featureId;

    /**
     * 用例名称
     */
    private String featureName;

    /**
     * 用例的测试步骤
     */
    private String testStep;

    /**
     * 父节点Id
     */
    private String parentId;

    /**
     * 用例类型用来识别是目录还是用例
     */
    private Integer featureType;

    /**
     * 用例状态
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sortOrder;

    private Long createTime;

    private Long updateTime;
}
