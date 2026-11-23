package com.zj.common.entity.command;

import com.zj.contract.feature.exec.BatchFeatureVo;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建用例任务记录的命令。
 *
 * 临时批量执行也需要一条 task_record 作为执行容器，但 task_record 的写权在 master，
 * 所以 console 只下发命令并拿回 recordId。
 *
 * @author guyuelan
 */
@Data
public class TaskRecordCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 触发来源ID
     */
    private String triggerId;

    /**
     * 记录类型，取 TaskRecordType 的 type
     */
    private Integer type;

    /**
     * 任务配置
     */
    private BatchFeatureVo taskConfig;

    /**
     * 用例集ID
     */
    private String testCaseId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 初始状态，取 ProcessStatus 的 type
     */
    private Integer status;

    /**
     * 发起人
     */
    private String userId;
}
