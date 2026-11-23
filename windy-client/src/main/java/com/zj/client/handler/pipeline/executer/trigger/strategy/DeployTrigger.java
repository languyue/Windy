package com.zj.client.handler.pipeline.executer.trigger.strategy;

import com.zj.client.handler.pipeline.deploy.DeployFactory;
import com.zj.client.handler.pipeline.deploy.DeployModeContext;
import com.zj.client.handler.pipeline.deploy.IDeployMode;
import com.zj.client.handler.pipeline.deploy.jar.JarDeployContext;
import com.zj.client.handler.pipeline.deploy.k8s.K8sDeployContext;
import com.zj.client.handler.pipeline.executer.trigger.INodeTrigger;
import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel;
import com.zj.client.handler.pipeline.executer.vo.RefreshContext;
import com.zj.client.handler.pipeline.executer.vo.TaskNode;
import com.zj.contract.enums.DeployType;
import com.zj.common.enums.ExecuteType;
import com.zj.common.exception.ExecuteException;
import com.zj.common.utils.GitUtils;
import com.zj.contract.json.ContractJson;
import com.zj.contract.pipeline.DeployParams;
import com.zj.contract.pipeline.K8SAccessParams;
import com.zj.contract.pipeline.SSHParams;
import com.zj.contract.pipeline.ServiceConfig;
import com.zj.contract.pipeline.node.DeployContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Jar部署处理
 *
 * @author guyuelan
 * @since 2023/5/8
 */
@Slf4j
@Component
public class DeployTrigger implements INodeTrigger<DeployContext> {
    private final DeployFactory deployFactory;

    private final Map<Integer, Function<DeployContext, DeployModeContext>> functionMap = new HashMap<>();

    public DeployTrigger(DeployFactory deployFactory) {
        this.deployFactory = deployFactory;
        functionMap.put(DeployType.SSH.getType(), this::buildSSHContext);
        functionMap.put(DeployType.K8S.getType(), this::buildK8SContext);
    }

    @Override
    public ExecuteType type() {
        return ExecuteType.DEPLOY;
    }

    @Override
    public void triggerRun(DeployContext context, TaskNode taskNode) {
        log.info("start run deploy context = {}", ContractJson.write(context));
        Function<DeployContext, DeployModeContext> function = functionMap.get(context.getDeployType());
        if (Objects.isNull(function)) {
            throw new ExecuteException("can not find deploy type");
        }
        DeployModeContext deployContext = function.apply(context);
        deployContext.setRecordId(taskNode.getRecordId());

        IDeployMode deployMode = deployFactory.getDeployMode(context.getDeployType());
        deployMode.deploy(deployContext);
    }

    @Override
    public QueryResponseModel queryStatus(RefreshContext refreshContext, TaskNode taskNode) {
        return deployFactory.getDeployStatus(taskNode.getRecordId());
    }

    private DeployModeContext buildSSHContext(DeployContext context) {
        SSHParams sshParams = context.getParams().getSshParams();
        return JarDeployContext.builder().sshUser(sshParams.getUser()).sshPassword(sshParams.getPassword())
                .remotePath(sshParams.getRemotePath()).sshIp(sshParams.getSshIp()).sshPort(sshParams.getSshPort())
                .localPath(context.getBuildFilePath()).servicePort(context.getServerPort()).build();
    }

    private DeployModeContext buildK8SContext(DeployContext context) {
        String serviceName = GitUtils.getServiceFromUrl(context.getGitUrl());
        DeployParams deployParams = context.getParams();
        K8SAccessParams k8SAccessParams = deployParams.getK8SAccessParams();
        ServiceConfig serviceConfig = deployParams.getServiceConfig();
        return K8sDeployContext.builder().serviceConfig(serviceConfig).k8SAccessParams(k8SAccessParams)
                .serviceName(serviceName).build();
    }
}
