package com.zj.domain.repository.service;

import com.zj.domain.entity.bo.service.DeployEnvironmentBO;
import java.util.List;
import com.zj.domain.repository.marker.ConfigCommandRepository;


/**
 * @author guyuelan
 */
public interface IEnvironmentCommandRepository extends IEnvironmentQueryRepository, ConfigCommandRepository {

  /**
  * 创建环境
  * @param deployEnvironment 环境信息
  * @return 是否成功
  */
  Boolean createEnvironment(DeployEnvironmentBO deployEnvironment);

  /**
  * 更新环境
  * @param deployEnvironment 环境信息
  * @return 是否成功
  */
  Boolean updateEnvironment(DeployEnvironmentBO deployEnvironment);

  /**
  * 删除环境
  * @param envId 环境ID
  * @return 是否成功
  */
  Boolean deleteEnvironment(String envId);

}
