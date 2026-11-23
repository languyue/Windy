package com.zj.domain.repository.service;

import com.zj.domain.entity.bo.service.ServiceGenerateBO;
import com.zj.domain.repository.marker.ConfigCommandRepository;


/**
 * @author guyuelan
 */
public interface IGenerateCommandRepository extends IGenerateQueryRepository, ConfigCommandRepository {

  /**
  * 创建服务生成信息
  * @param serviceGenerateBO 生成信息
  * @return 是否成功
  */
  boolean create(ServiceGenerateBO serviceGenerateBO);

  /**
  * 更新服务生成信息
  * @param serviceGenerateBO 生成信息
  * @return 是否成功
  */
  boolean update(ServiceGenerateBO serviceGenerateBO);

}
