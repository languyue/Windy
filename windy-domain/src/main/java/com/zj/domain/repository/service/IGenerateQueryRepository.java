package com.zj.domain.repository.service;

import com.zj.domain.entity.bo.service.ServiceGenerateBO;


/**
 * @author guyuelan
 */
public interface IGenerateQueryRepository {

  /**
  * 获取服务生成信息
  * @param serviceId 服务ID
  * @return 生成信息
  */
  ServiceGenerateBO getByService(String serviceId);

}
