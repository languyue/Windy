package com.zj.common.adapter.invoker.eureka;

import com.zj.common.adapter.invoker.IMasterCommand;
import com.zj.common.entity.command.ApproveCommand;
import com.zj.common.entity.command.PipelineHistoryCommand;
import com.zj.common.entity.command.TaskRecordCommand;
import com.zj.contract.rpc.ResponseMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * master 运行态命令的远端实现。
 *
 * console 不写运行态表，需要改变 node_record、pipeline_history 等表时把命令下发给 master，
 * 由 master 的 MasterCommandRest 落地。
 *
 * @author guyuelan
 */
@Slf4j
public class EurekaMasterCommandAdapter extends BaseEurekaAdapter implements IMasterCommand {

    private static final String APPROVE_NODE = "http://WindyMaster/v1/devops/master/command/approve";
    private static final String CREATE_PIPELINE_HISTORY = "http://WindyMaster/v1/devops/master/command/history";
    private static final String CREATE_TASK_RECORD = "http://WindyMaster/v1/devops/master/command/task-record";
    private static final String DELETE_PIPELINE_HISTORY = "http://WindyMaster/v1/devops/master/command/history/%s";
    private static final String DELETE_NODE_RECORDS = "http://WindyMaster/v1/devops/master/command/node-record";
    private static final String DELETE_TASK_RECORD = "http://WindyMaster/v1/devops/master/command/task-record/%s";
    private static final String DELETE_FEATURE_HISTORY = "http://WindyMaster/v1/devops/master/command/feature-history/%s";

    private static final ParameterizedTypeReference<ResponseMeta<Boolean>> META_BOOLEAN =
            new ParameterizedTypeReference<ResponseMeta<Boolean>>() {};
    private static final ParameterizedTypeReference<ResponseMeta<String>> META_STRING =
            new ParameterizedTypeReference<ResponseMeta<String>>() {};

    public EurekaMasterCommandAdapter(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    public boolean approveNode(ApproveCommand command) {
        return postBoolean(APPROVE_NODE, command);
    }

    @Override
    public String createPipelineHistory(PipelineHistoryCommand command) {
        ResponseMeta<String> meta = exchange(CREATE_PIPELINE_HISTORY, HttpMethod.POST, command, META_STRING);
        return Optional.ofNullable(meta).map(ResponseMeta::getData).orElse(null);
    }

    @Override
    public String createTaskRecord(TaskRecordCommand command) {
        ResponseMeta<String> meta = exchange(CREATE_TASK_RECORD, HttpMethod.POST, command, META_STRING);
        return Optional.ofNullable(meta).map(ResponseMeta::getData).orElse(null);
    }

    @Override
    public boolean deletePipelineHistory(String pipelineId) {
        return deleteBoolean(String.format(DELETE_PIPELINE_HISTORY, pipelineId));
    }

    @Override
    public boolean deleteNodeRecords(List<String> nodeIds) {
        return postBoolean(DELETE_NODE_RECORDS, nodeIds);
    }

    @Override
    public boolean deleteTaskRecord(String recordId) {
        return deleteBoolean(String.format(DELETE_TASK_RECORD, recordId));
    }

    @Override
    public boolean deleteFeatureHistory(String historyId) {
        return deleteBoolean(String.format(DELETE_FEATURE_HISTORY, historyId));
    }

    private boolean postBoolean(String url, Object body) {
        ResponseMeta<Boolean> meta = exchange(url, HttpMethod.POST, body, META_BOOLEAN);
        return Objects.nonNull(meta) && Boolean.TRUE.equals(meta.getData());
    }

    private boolean deleteBoolean(String url) {
        ResponseMeta<Boolean> meta = exchange(url, HttpMethod.DELETE, null, META_BOOLEAN);
        return Objects.nonNull(meta) && Boolean.TRUE.equals(meta.getData());
    }
}
