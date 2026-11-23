package com.zj.domain.repository.service;

import com.zj.domain.entity.bo.service.BuildToolBO;
import java.util.List;
import com.zj.domain.repository.marker.ConfigCommandRepository;


/**
 * @author guyuelan
 */
public interface IBuildToolCommandRepository extends IBuildToolQueryRepository, ConfigCommandRepository {

  /**
  * 保存构建工具
  * @param buildTool 构建工具信息
  * @return 是否成功
  */
  boolean saveBuildTool(BuildToolBO buildTool);

  /**
  * 删除构建工具
  * @param buildToolId 构建工具ID
  * @return 是否成功
  */
  boolean deleteBuildTool(String buildToolId);

  /**
  * 更新构建工具
  * @param buildToolBO 构建工具信息
  * @return 是否成功
  */
  boolean updateBuildTool(BuildToolBO buildToolBO);

}
