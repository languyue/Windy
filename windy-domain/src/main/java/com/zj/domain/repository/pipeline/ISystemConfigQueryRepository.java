package com.zj.domain.repository.pipeline;

import com.zj.contract.git.GitAccessInfo;
import com.zj.domain.entity.bo.pipeline.SystemConfigBO;
import com.zj.domain.entity.vo.DefaultPipelineVo;
import com.zj.domain.entity.vo.ImageRepositoryVo;
import com.zj.domain.entity.vo.GenerateMavenConfigDto;
import java.util.List;

/**
* @author guyuelan
* @since 2023/5/19
*/
public interface ISystemConfigQueryRepository {

  /**
  * 获取所有配置
  * @return 配置列表
  */
  List<SystemConfigBO> getAllConfigs();

  /**
  * 获取配置
  * @param configId 配置ID
  * @return 配置信息
  */
  SystemConfigBO getSystemConfig(String configId);

  /**
  * 获取Git访问配置
  * @return 系统Git配置信息
  */
  GitAccessInfo getGitAccess();

  /**
  * 获取镜像仓库配置
  * @return 镜像仓库配置信息
  */
  ImageRepositoryVo getImageRepository();

  /**
  * 获取默认流水线配置
  * @return 默认流水线配置信息
  */
  DefaultPipelineVo getDefaultPipeline();

  /**
  * 获取二方包maven配置
  * @return maven配置信息
  */
  GenerateMavenConfigDto getMavenConfig();

}
