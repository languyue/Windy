package com.zj.client.handler.pipeline.deploy.k8s;

import com.alibaba.fastjson.JSON;
import com.zj.client.handler.pipeline.deploy.AbstractDeployMode;
import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel;
import com.zj.client.utils.ExceptionUtils;
import com.zj.common.enums.DeployType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.exception.ExecuteException;
import com.zj.common.entity.pipeline.K8SAccessParams;
import com.zj.common.entity.pipeline.ServiceConfig;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.LocalObjectReference;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpecBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentStatus;
import io.fabric8.kubernetes.api.model.apps.DeploymentStrategy;
import io.fabric8.kubernetes.client.Client;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author falcon
 * @since 2023/7/26
 */
@Slf4j
@Component
public class K8sDeploy extends AbstractDeployMode<K8sDeployContext> {

    public static final int DEFAULT_REPLICAS = 1;
    public static final int MAX_DEPLOY_TIME = 300 * 1000;
    public static final String LABEL_APP_KEY = "app";

    @Override
    public Integer deployType() {
        return DeployType.K8S.getType();
    }

    @Override
    public void deploy(K8sDeployContext deployContext) {
        K8SAccessParams k8SAccess = deployContext.getK8SAccessParams();
        //部署代码
        Config config = new ConfigBuilder().withMasterUrl(k8SAccess.getApiService())
                .withNamespace(k8SAccess.getNamespace())
                .withTrustCerts(true).withOauthToken(k8SAccess.getToken()).build();
        try (KubernetesClient client = new DefaultKubernetesClient(config)){
            String appName = Optional.ofNullable(deployContext.getServiceConfig().getAppName())
                    .orElse(deployContext.getServiceName()).toLowerCase();
            List<String> messages = convertDeployInfo(deployContext, appName);
            updateDeployStatus(deployContext.getRecordId(), ProcessStatus.RUNNING, messages);
            deployK8s(deployContext, client, appName);
            loopQueryDeployStatus(deployContext, client, appName);
            updateDeployStatus(deployContext.getRecordId(), ProcessStatus.SUCCESS);
        } catch (Exception e) {
            log.error("deploy k8s instance error", e);
            List<String> errorMsg = ExceptionUtils.getErrorMsg(e);
            updateDeployStatus(deployContext.getRecordId(), ProcessStatus.FAIL, errorMsg);
        }
    }

    private List<String> convertDeployInfo(K8sDeployContext deployContext, String appName) {
        List<String> messages = new ArrayList<>();
        messages.add("start deploy deployment:");
        messages.add("app name: " + appName);
        messages.add("NameSpace: " + deployContext.getK8SAccessParams().getNamespace());
        ServiceConfig serviceConfig = deployContext.getServiceConfig();
        messages.add("replicas: " + serviceConfig.getReplicas());
        messages.add("image address: " + serviceConfig.getImageName());
        messages.add("service port: " + JSON.toJSONString(serviceConfig.getPorts()));
        messages.add("app environment: " + JSON.toJSONString(serviceConfig.getEnvParams()));
        messages.add("volumes: " + JSON.toJSONString(serviceConfig.getVolumes()));
        return messages;
    }

    private void deployK8s(K8sDeployContext deployContext, KubernetesClient client, String appName) {
        Deployment deployment = client.apps().deployments().inNamespace(deployContext.getK8SAccessParams().getNamespace())
                .withName(appName).get();
        if (Objects.nonNull(deployment)) {
            log.info("deployment exist start edit ={}", appName);
            editDeployment(client, appName, deployContext, deployment);
            return;
        }
        deployNewApp(deployContext, client, appName);
    }

    private void deployNewApp(K8sDeployContext deployContext, KubernetesClient client, String appName) {
        ServiceConfig containerParams = deployContext.getServiceConfig();
        List<EnvVar> envVars = buildEnvParams(containerParams.getEnvParams());
        //宿主机路径
        List<Volume> volumes = buildVolumes(containerParams.getVolumes());
        //容器路径
        List<VolumeMount> volumeMounts = buildVolumeMounts(containerParams.getVolumes());
        //端口映射
        List<ContainerPort> containerPorts = buildContainerPorts(containerParams.getPorts());
        Integer replicas = Optional.ofNullable(containerParams.getReplicas()).orElse(DEFAULT_REPLICAS);

        Container container =
                new ContainerBuilder().withEnv(envVars).withName(appName).withImage(containerParams.getImageName())
                        .withPorts(containerPorts)
                        .withVolumeMounts(volumeMounts).build();

        K8SAccessParams k8SAccessParams = deployContext.getK8SAccessParams();
        PodTemplateSpec podTemplate = new PodTemplateSpecBuilder().withNewMetadata().addToLabels(LABEL_APP_KEY,
                        appName).endMetadata().withNewSpec()
                .withVolumes(volumes)
                .withImagePullSecrets(new LocalObjectReference(k8SAccessParams.getSecretName()))
                .withContainers(container).endSpec().build();

        DeploymentStrategy strategy = new DeploymentStrategy();
        strategy.setType(containerParams.getStrategy().getType());

        Deployment deployment =
                new DeploymentBuilder().withNewMetadata().withName(appName)
                        .addToLabels(LABEL_APP_KEY, appName).endMetadata()
                        .withNewSpec().withNewSelector()
                        .addToMatchLabels(LABEL_APP_KEY, appName).endSelector()
                        .withReplicas(replicas).withStrategy(strategy)
                        .withTemplate(podTemplate).endSpec().build();

        log.info("image name={}", containerParams.getImageName());
        updateDeployStatus(deployContext.getRecordId(), ProcessStatus.RUNNING);
        String namespace = k8SAccessParams.getNamespace();
        client.apps().deployments().inNamespace(namespace).createOrReplace(deployment);
    }

    public void editDeployment(KubernetesClient client, String appName, K8sDeployContext deployContext,
                               Deployment deployment) {
        int index = getContainerIndex(appName, deployment);
        String imageName = deployContext.getServiceConfig().getImageName();
        client.apps().deployments().inNamespace(deployContext.getK8SAccessParams().getNamespace())
                .withName(appName)
                .edit(deploy ->
                        new DeploymentBuilder(deploy).editSpec()
                                .editTemplate()
                                .editSpec()
                                .editContainer(index)
                                .withImage(imageName)
                                .endContainer().endSpec().endTemplate().endSpec().build());
    }

    private int getContainerIndex(String appName, Deployment deployment) {
        int index = 0;
        PodTemplateSpec template = deployment.getSpec().getTemplate();
        if (Objects.nonNull(template) && Objects.nonNull(template.getSpec()) && Objects.nonNull(template.getSpec().getContainers())) {
            List<Container> containers = deployment.getSpec().getTemplate().getSpec().getContainers();
            index = IntStream.range(0, containers.size()).filter(i -> Objects.equals(containers.get(i).getName(),
                    appName)).findFirst().orElse(0);
            log.info("find container index = {}", index);
        }
        return index;
    }

    private void loopQueryDeployStatus(K8sDeployContext deployContext, KubernetesClient client,
                                       String appName) throws InterruptedException {
        AtomicLong atomicLong = new AtomicLong(0);
        while (true) {
            long time = atomicLong.addAndGet(10000);
            Thread.sleep(time);
            String namespace = deployContext.getK8SAccessParams().getNamespace();
            Deployment deploy = client.apps().deployments().inNamespace(namespace)
                    .withName(appName).get();
            DeploymentStatus status = deploy.getStatus();
            if (Objects.equals(status.getReplicas(), status.getAvailableReplicas())) {
                break;
            }

            if (atomicLong.get() >= MAX_DEPLOY_TIME) {
                throw new ExecuteException("部署时间超时");
            }
        }
    }

    @Override
    public QueryResponseModel getDeployStatus(String recordId) {
        return statusMap.get(recordId);
    }

    private List<EnvVar> buildEnvParams(List<ServiceConfig.ContainerEnv> envParams) {
        if (CollectionUtils.isEmpty(envParams)) {
            return Collections.emptyList();
        }
        return envParams.stream().filter(ServiceConfig.ContainerEnv::notExistEmpty).map(this::buildEnvItem)
                .collect(Collectors.toList());
    }

    private EnvVar buildEnvItem(ServiceConfig.ContainerEnv containerEnv) {
        if (containerEnv.isRelated()) {
            return new EnvVarBuilder().withName(containerEnv.getName()).withNewValueFrom()
                    .withNewFieldRef().withFieldPath(containerEnv.getValue()).endFieldRef().endValueFrom()
                    .build();
        }
        return new EnvVarBuilder().withName(containerEnv.getName()).withValue(containerEnv.getValue())
                .build();
    }

    private List<VolumeMount> buildVolumeMounts(List<ServiceConfig.ContainerVolume> volumes) {
        if (CollectionUtils.isEmpty(volumes)) {
            return Collections.emptyList();
        }
        return volumes.stream().filter(ServiceConfig.ContainerVolume::notExistEmpty).map(
                volume -> new VolumeMountBuilder().withName(volume.getName())
                        .withMountPath(volume.getVolume()).build()).collect(Collectors.toList());
    }

    private List<Volume> buildVolumes(List<ServiceConfig.ContainerVolume> volumes) {
        if (CollectionUtils.isEmpty(volumes)) {
            return Collections.emptyList();
        }
        return volumes.stream().filter(ServiceConfig.ContainerVolume::notExistEmpty).map(
                volume -> new VolumeBuilder().withName(volume.getName())
                        .withNewHostPath(volume.getHostVolume(), "").build()).collect(Collectors.toList());
    }

    private List<ContainerPort> buildContainerPorts(List<ServiceConfig.ContainerPort> ports) {
        if (CollectionUtils.isEmpty(ports)) {
            return Collections.emptyList();
        }
        return ports.stream().filter(ServiceConfig.ContainerPort::notExistEmpty).map(
                        port -> new ContainerPortBuilder().withContainerPort(port.getPort())
                                .withHostPort(port.getHostPort()).withProtocol(port.getProtocol()).build())
                .collect(Collectors.toList());
    }
}
