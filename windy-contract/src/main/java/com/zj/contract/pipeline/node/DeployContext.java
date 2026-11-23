package com.zj.contract.pipeline.node;

import com.zj.contract.pipeline.DeployParams;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/6/15
 */
@Data
public class DeployContext extends RequestContext{

  private String envId;

  private DeployParams params;

  private Integer deployType;

  private Integer serverPort;

  private String imageName;

  private String buildFilePath;

}
