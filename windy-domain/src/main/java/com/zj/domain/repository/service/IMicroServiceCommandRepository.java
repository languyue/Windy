package com.zj.domain.repository.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.domain.entity.bo.service.MicroserviceBO;
import java.util.List;
import com.zj.domain.repository.marker.ConfigCommandRepository;

/**
* @author guyuelan
* @since 2023/6/20
*/
public interface IMicroServiceCommandRepository extends IMicroServiceQueryRepository, ConfigCommandRepository {

  /**
  * 创建服务
  * @param userId 用户ID
  * @param microserviceBO 服务信息
  * @return 服务ID
  */
  String createService(String userId, MicroserviceBO microserviceBO);

  /**
  * 更新服务
  * @param microserviceBO 服务信息
  * @return 是否成功
  */
  boolean updateService(MicroserviceBO microserviceBO);

  /**
  * 删除服务
  * @param serviceId 服务ID
  * @return 是否成功
  */
  Boolean deleteService(String serviceId);

}
