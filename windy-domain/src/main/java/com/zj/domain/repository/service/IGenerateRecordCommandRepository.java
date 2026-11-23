package com.zj.domain.repository.service;

import com.zj.common.entity.generate.GenerateRecordBO;
import java.util.List;
import com.zj.domain.repository.marker.RuntimeCommandRepository;


/**
 * @author guyuelan
 */
public interface IGenerateRecordCommandRepository extends IGenerateRecordQueryRepository, RuntimeCommandRepository {

  /**
  * 创建二方包生成记录
  * @param generateRecordBO 生成记录
  * @return 是否成功
  */
  boolean create(GenerateRecordBO generateRecordBO);

  /**
  * 更新二方包生成记录
  * @param generateRecordBO 生成记录
  * @return 是否成功
  */
  boolean update(GenerateRecordBO generateRecordBO);

}
