package com.zj.client.handler.pipeline.deploy;

import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel;
import com.zj.contract.enums.ProcessStatus;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractDeployMode<T extends DeployModeContext> implements IDeployMode<T>{

    protected final Map<String, QueryResponseModel> statusMap = new HashMap<>();

    public void updateDeployStatus(String recordId, ProcessStatus processStatus) {
        statusMap.put(recordId, convertStatus(processStatus, recordId));
    }

    public void updateDeployStatus(String recordId, ProcessStatus processStatus, List<String> messages) {
        statusMap.put(recordId, convertStatus(processStatus,recordId, messages));
    }

    private QueryResponseModel convertStatus(ProcessStatus processStatus, String recordId){
        QueryResponseModel queryResponseModel = new QueryResponseModel();
        if (statusMap.containsKey(recordId)) {
            queryResponseModel = statusMap.get(recordId);
        }
        queryResponseModel.setStatus(processStatus.getType());
        queryResponseModel.addMessage(processStatus.getDesc());
        queryResponseModel.setData(Collections.singletonMap("status", processStatus.getType()));
        return queryResponseModel;
    }

    private QueryResponseModel convertStatus(ProcessStatus processStatus, String recordId, List<String> messages){
        QueryResponseModel queryResponseModel = new QueryResponseModel();
        if (statusMap.containsKey(recordId)) {
            queryResponseModel = statusMap.get(recordId);
        }
        queryResponseModel.setStatus(processStatus.getType());
        queryResponseModel.addAllMessage(messages);
        queryResponseModel.setData(Collections.singletonMap("status", processStatus.getType()));
        return queryResponseModel;
    }
}
