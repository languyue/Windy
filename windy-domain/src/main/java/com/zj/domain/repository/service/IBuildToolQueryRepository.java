package com.zj.domain.repository.service;

import com.zj.domain.entity.bo.service.BuildToolBO;
import java.util.List;


/**
 * @author guyuelan
 */
public interface IBuildToolQueryRepository {

  /**
  * 获取构建工具列表
  * @return 构建工具列表
  */
  List<BuildToolBO> getBuildToolList();

}
