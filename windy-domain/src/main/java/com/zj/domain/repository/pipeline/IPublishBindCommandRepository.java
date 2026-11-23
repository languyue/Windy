package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.bo.pipeline.PublishBindBO;
import java.util.List;
import com.zj.domain.repository.marker.ConfigCommandRepository;

/**
* @author guyuelan
* @since 2023/6/28
*/
public interface IPublishBindCommandRepository extends IPublishBindQueryRepository, ConfigCommandRepository {

  /**
  * 创建流水线发布
  * @param publishBind 发布信息
  * @return 是否成功
  */
  boolean createPublish(PublishBindBO publishBind);

  /**
  * 更新流水线发布
  * @param publishBind 发布信息
  * @return 是否成功
  */
  boolean updatePublish(PublishBindBO publishBind);

  /**
  * 删除流水线发布
  * @param publishId 发布ID
  * @return 是否成功
  */
  boolean deletePublish(String publishId);

  /**
  * 删除服务发布信息
  * @param serviceId 服务ID
  * @return 是否成功
  */
  boolean deleteServicePublishes(String serviceId);

}
