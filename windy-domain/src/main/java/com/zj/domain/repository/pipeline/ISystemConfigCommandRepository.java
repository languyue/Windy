package com.zj.domain.repository.pipeline;

import com.zj.contract.git.GitAccessInfo;
import com.zj.domain.entity.bo.pipeline.SystemConfigBO;
import com.zj.domain.entity.vo.DefaultPipelineVo;
import com.zj.domain.entity.vo.ImageRepositoryVo;
import com.zj.domain.entity.vo.GenerateMavenConfigDto;
import java.util.List;
import com.zj.domain.repository.marker.ConfigCommandRepository;

/**
* @author guyuelan
* @since 2023/5/19
*/
public interface ISystemConfigCommandRepository extends ISystemConfigQueryRepository, ConfigCommandRepository {

  /**
  * 保存配置
  * @param systemConfigBO 配置信息
  * @return 是否成功
  */
  boolean saveConfig(SystemConfigBO systemConfigBO);

  /**
  * 更新配置
  * @param systemConfigBO 配置信息
  * @return 是否成功
  */
  boolean updateConfig(SystemConfigBO systemConfigBO);

  /**
  * 删除配置
  * @param configId 配置ID
  * @return 是否成功
  */
  boolean deleteConfig(String configId);

  /**
  * 更新Git访问配置
  * @param gitAccess Git配置信息
  * @return 是否成功
  */
  boolean updateGitAccess(GitAccessInfo gitAccess);

  /**
  * 更新镜像仓库配置
  * @param imageRepositoryVo 镜像仓库配置信息
  * @return 是否成功
  */
  boolean updateImageRepository(ImageRepositoryVo imageRepositoryVo);

  /**
  * 更新二方包maven配置
  * @param mavenConfig maven配置信息
  * @return 是否成功
  */
  boolean updateMavenConfig(GenerateMavenConfigDto mavenConfig);

}
