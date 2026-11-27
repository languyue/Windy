package com.zj.contract.rpc.command;

import com.zj.contract.pipeline.PipelineConfig;
import lombok.Data;

import java.io.Serializable;

/**
 * 新建流水线运行历史的命令。
 *
 * pipeline_history 属于运行态域，只有 master 能写；console 侧的"重跑/手工建历史"一律下发本命令。
 *
 * @author guyuelan
 */
@Data
public class PipelineHistoryCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 流水线ID
     */
    private String pipelineId;

    /**
     * 流水线名称
     */
    private String pipelineName;

    /**
     * 流水线执行的分支
     */
    private String branch;

    /**
     * 流水线执行时的配置快照
     */
    private PipelineConfig pipelineConfig;

    /**
     * 初始流程状态，取 ProcessStatus 的 type
     */
    private Integer pipelineStatus;
}
