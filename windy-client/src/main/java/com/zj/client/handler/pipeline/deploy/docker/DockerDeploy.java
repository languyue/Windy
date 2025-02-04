package com.zj.client.handler.pipeline.deploy.docker;

import com.zj.client.handler.pipeline.deploy.AbstractDeployMode;
import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel;
import com.zj.common.enums.DeployType;
import org.springframework.stereotype.Component;

/**
 * k8s镜像部署
 * @author guyuelan
 * @since 2023/6/8
 */
@Component
public class DockerDeploy extends AbstractDeployMode<DockerDeployContext> {

  @Override
  public Integer deployType() {
    return DeployType.DOCKER.getType();
  }

  @Override
  public void deploy(DockerDeployContext deployContext) {

  }

  @Override
  public QueryResponseModel getDeployStatus(String recordId) {
    return null;
  }
}
