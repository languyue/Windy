package com.zj.domain.repository.service;

import com.zj.domain.entity.bo.service.ServiceApiBO;
import java.util.List;

/**
* @author guyuelan
* @since 2023/8/8
*/
public interface IServiceApiQueryRepository {

  /**
  * 获取API详情
  * @param apiId API ID
  * @return API信息
  */
  ServiceApiBO getServiceApi(String apiId);

  /**
  * 获取服务下的API
  * @param serviceId 服务ID
  * @return API列表
  */
  List<ServiceApiBO> getApiByService(String serviceId);

  /**
  * 获取API列表
  * @param apiIds API ID列表
  * @return API列表
  */
  List<ServiceApiBO> getServiceApiList(List<String> apiIds);

}
