package com.zj.demand.entity;

import com.zj.domain.entity.vo.Create;
import com.zj.domain.entity.vo.Update;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class WorkTaskDto {

    @NotBlank(groups = Update.class)
    private String taskId;

    /**
     * 任务名称
     */
    @Length(min = 4, max = 50)
    @NotBlank(groups = Create.class)
    private String taskName;

    /**
     * 任务描述
     */
    @Length(max = 256)
    private String description;

    /**
     * 关联Id
     */
    @NotBlank(groups = Create.class)
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
}
