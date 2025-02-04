package com.zj.domain.entity.bo.demand;

import lombok.Data;

@Data
public class WorkTaskBO {

    private Long id;

    /**
     * 工作任务Id
     */
    private String taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 任务创建人
     */
    private String creator;

    /**
     * 关联Id
     */
    private String relatedId;

    /**
     * 关联类型
     */
    private Integer relatedType;

    /**
     * 工作量
     */
    private Integer workload;

    private Integer status;

    /**
     * 完成时间
     */
    private Long completeTime;

    private Long createTime;

    private Long updateTime;
}
