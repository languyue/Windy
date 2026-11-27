package com.zj.contract.rpc.command;

import lombok.Data;

import java.io.Serializable;

/**
 * 审批命令。
 *
 * 审批结论由 console 的界面操作产生，但 node_record 与 approval_request 的写权在 master，
 * 所以 console 只能下发命令而不能直接写表。
 *
 * @author guyuelan
 */
@Data
public class ApproveCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 节点记录ID
     */
    private String recordId;

    /**
     * 流水线历史ID
     */
    private String historyId;

    /**
     * 节点ID
     */
    private String nodeId;

    /**
     * 审批结论对应的流程状态，取 ProcessStatus 的 type
     */
    private Integer status;

    /**
     * 审批意见
     */
    private String comment;

    /**
     * 审批人
     */
    private String approver;
}
