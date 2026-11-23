package com.zj.domain.repository.service;

import com.zj.domain.entity.bo.service.ServiceApiBO;
import java.util.List;
import com.zj.domain.repository.marker.ConfigCommandRepository;

/**
* @author guyuelan
* @since 2023/8/8
*/
public interface IServiceApiCommandRepository extends IServiceApiQueryRepository, ConfigCommandRepository {

  /**
  * 保存API
  * @param serviceApi API信息
  * @return 是否成功
  */
  boolean saveApi(ServiceApiBO serviceApi);

  /**
  * 批量保存API
  * @param serviceApis API信息列表
  * @return 是否成功
  */
  boolean saveBatch(List<ServiceApiBO> serviceApis);

  /**
  * 更新API
  * @param serviceApi API信息
  * @return 是否成功
  */
  boolean updateApi(ServiceApiBO serviceApi);

  /**
  * 删除API
  * @param apiId API ID
  * @return 是否成功
  */
  boolean deleteApi(String apiId);

  /**
  * 批量删除API
  * @param apiIds API ID列表
  * @return 是否成功
  */
  boolean batchDeleteApi(List<String> apiIds);

}
