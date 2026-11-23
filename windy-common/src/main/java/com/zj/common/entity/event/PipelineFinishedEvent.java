package com.zj.common.entity.event;

import lombok.Data;

import java.io.Serializable;

/**
 * 流水线执行结束事件。
 *
 * master 只负责宣告"某条流水线以某种状态结束了"，需求、缺陷、工作项、代码变更、发布绑定这些
 * 业务域的后续状态怎么变，是 console 的业务规则，因此由 console 监听本事件自行更新。
 *
 * @author guyuelan
 */
@Data
public class PipelineFinishedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 流水线ID
     */
    private String pipelineId;

    /**
     * 本次执行的历史ID
     */
    private String historyId;

    /**
     * 流水线归属的服务ID
     */
    private String serviceId;

    /**
     * 流水线类型，取 PipelineType 的 type
     */
    private Integer pipelineType;

    /**
     * 结束时的流程状态，取 ProcessStatus 的 type
     */
    private Integer processStatus;
}
