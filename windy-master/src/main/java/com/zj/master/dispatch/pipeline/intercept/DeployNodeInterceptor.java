package com.zj.master.dispatch.pipeline.intercept;

import com.zj.common.entity.WindyConstants;
import com.zj.common.enums.ExecuteType;
import com.zj.contract.pipeline.DeployParams;
import com.zj.contract.pipeline.K8SAccessParams;
import com.zj.contract.pipeline.SSHParams;
import com.zj.contract.pipeline.ServiceConfig;
import com.zj.domain.entity.bo.log.DispatchLogBO;
import com.zj.domain.entity.bo.log.SubDispatchLogBO;
import com.zj.domain.entity.bo.pipeline.NodeRecordBO;
import com.zj.domain.entity.bo.service.DeployEnvironmentBO;
import com.zj.domain.entity.bo.service.MicroserviceBO;
import com.zj.domain.entity.enums.EnvType;
import com.zj.domain.repository.log.IDispatchLogQueryRepository;
import com.zj.domain.repository.log.ISubDispatchLogQueryRepository;
import com.zj.domain.repository.pipeline.INodeRecordQueryRepository;
import com.zj.domain.repository.service.IEnvironmentQueryRepository;
import com.zj.domain.repository.service.IMicroServiceQueryRepository;
import com.zj.contract.pipeline.node.DeployContext;
import com.zj.contract.pipeline.node.TaskNode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 部署节点前置处理
 *
 * @author guyuelan
 * @since 2023/6/15
 */
@Component
public class DeployNodeInterceptor implements INodeExecuteInterceptor {
    private final ISubDispatchLogQueryRepository subDispatchLogRepository;
    private final IEnvironmentQueryRepository environmentRepository;
    private final IDispatchLogQueryRepository dispatchLogRepository;
    private final INodeRecordQueryRepository nodeRecordRepository;
    private final IMicroServiceQueryRepository microServiceRepository;

    public DeployNodeInterceptor(ISubDispatchLogQueryRepository subDispatchLogRepository,
                                 IEnvironmentQueryRepository environmentRepository,
                                 IDispatchLogQueryRepository dispatchLogRepository,
                                 INodeRecordQueryRepository nodeRecordRepository,
                                 IMicroServiceQueryRepository microServiceRepository) {
        this.subDispatchLogRepository = subDispatchLogRepository;
        this.environmentRepository = environmentRepository;
        this.dispatchLogRepository = dispatchLogRepository;
        this.nodeRecordRepository = nodeRecordRepository;
        this.microServiceRepository = microServiceRepository;
    }

    @Override
    public int sort() {
        return 4;
    }

    @Override
    public void beforeExecute(TaskNode taskNode) {
        //部署节点运行需要知道之前构建节点在哪里， 否则在JAR部署场景就会出现找不到文件的问题
        if (!Objects.equals(taskNode.getExecuteType(), ExecuteType.DEPLOY.name())) {
            return;
        }
        List<SubDispatchLogBO> subLogs = subDispatchLogRepository.getSubLogByLogId(taskNode.getLogId());
        Optional<SubDispatchLogBO> optional = subLogs.stream()
                .filter(subLog -> Objects.equals(subLog.getExecuteType(), ExecuteType.BUILD.name()))
                .findFirst();
        DeployContext deployContext = (DeployContext) taskNode.getRequestContext();
        if (optional.isPresent()) {
            SubDispatchLogBO subDispatchLogBO = optional.get();
            wrapBuildContext(subDispatchLogBO, deployContext);

            //固定回到构建节点所在的 client 执行，保证构建产物在本节点可见
            taskNode.setSingleClientIp(subDispatchLogBO.getClientIp());
            taskNode.setRequestSingle(true);
        }

        DeployEnvironmentBO deployEnvironment = environmentRepository.getEnvironment(deployContext.getEnvId());
        DeployParams deployParams = getDeployParams(taskNode.getServiceId(), deployEnvironment, deployContext.getImageName());
        deployContext.setParams(deployParams);
        deployContext.setDeployType(deployEnvironment.getEnvType());
        deployContext.setServerPort(deployParams.getServiceConfig().getServiceContext().getServicePort());
        taskNode.setRequestContext(deployContext);
    }

    public DeployParams getDeployParams(String serviceId, DeployEnvironmentBO environment, String imageName) {
        DeployParams deployParams = new DeployParams();
        if (Objects.equals(environment.getEnvType(), EnvType.K8S.getType())) {
            deployParams.setK8SAccessParams((K8SAccessParams) environment.getAccessParams());
        }

        if (Objects.equals(environment.getEnvType(), EnvType.SSH.getType())) {
            deployParams.setSshParams((SSHParams) environment.getAccessParams());
        }

        MicroserviceBO serviceDetail = microServiceRepository.queryServiceDetail(serviceId);
        ServiceConfig serviceConfig = serviceDetail.getServiceConfig();
        serviceConfig.setImageName(imageName);
        deployParams.setServiceConfig(serviceConfig);
        return deployParams;
    }

    private void wrapBuildContext(SubDispatchLogBO subDispatchLogBO, DeployContext deployContext) {
        DispatchLogBO dispatchLog = dispatchLogRepository.getDispatchLog(
                subDispatchLogBO.getLogId());
        String nodeId = subDispatchLogBO.getExecuteId();
        String pipelineHistoryId = dispatchLog.getSourceRecordId();
        NodeRecordBO nodeRecord = nodeRecordRepository.getRecordsByNodeIdAndHistory(pipelineHistoryId,
                nodeId);
        Map<String, Object> context = nodeRecord.getPipelineContext();
        if (Objects.nonNull(context)) {
            deployContext.setImageName(String.valueOf(context.get(WindyConstants.IMAGE_NAME)));
            deployContext.setBuildFilePath(String.valueOf(context.get(WindyConstants.BUILD_FILE_PATH)));
        }
    }
}
