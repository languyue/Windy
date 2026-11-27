package com.zj.contract.port;

import com.zj.contract.rpc.command.ApproveCommand;
import com.zj.contract.rpc.command.PipelineHistoryCommand;
import com.zj.contract.rpc.command.TaskRecordCommand;

import java.util.List;

/**
 * console 向 master 下发的运行态命令。
 *
 * 运行态域（node_record、approval_request、pipeline_history、task_record、feature_history）只允许
 * master 写，console 需要改变这些表时必须走这里的命令，不能直接注入运行态的 CommandRepository。
 *
 * <p>跨 JVM 协议：调用方是 console，实现方是 master 的 MasterCommandRest。</p>
 *
 * @author guyuelan
 */
public interface IMasterCommand {

    /**
     * 提交审批结论：master 落审批工单并推进节点状态
     */
    boolean approveNode(ApproveCommand command);

    /**
     * 新建一条流水线执行历史，返回历史ID
     */
    String createPipelineHistory(PipelineHistoryCommand command);

    /**
     * 为一次临时批量执行创建任务记录，返回记录ID
     */
    String createTaskRecord(TaskRecordCommand command);

    /**
     * 删除流水线的所有执行历史
     */
    boolean deletePipelineHistory(String pipelineId);

    /**
     * 删除指定节点的执行记录
     */
    boolean deleteNodeRecords(List<String> nodeIds);

    /**
     * 删除用例任务记录
     */
    boolean deleteTaskRecord(String recordId);

    /**
     * 删除用例执行历史
     */
    boolean deleteFeatureHistory(String historyId);
}
