package com.zj.client.handler.pipeline.deploy.k8s;

import com.zj.client.handler.pipeline.deploy.DeployModeContext;
import com.zj.contract.pipeline.K8SAccessParams;
import com.zj.contract.pipeline.ServiceConfig;
import lombok.Builder;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/7/26
 */
@Data
@Builder
public class K8sDeployContext extends DeployModeContext {

  private String serviceName;

  private K8SAccessParams k8SAccessParams;

  private ServiceConfig serviceConfig;
}
