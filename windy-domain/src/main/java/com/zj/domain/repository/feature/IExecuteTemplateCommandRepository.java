package com.zj.domain.repository.feature;

import com.zj.domain.entity.bo.feature.ExecuteTemplateBO;
import java.util.List;
import com.zj.domain.repository.marker.ConfigCommandRepository;

/**
* @author guyuelan
* @since 2023/5/18
*/
public interface IExecuteTemplateCommandRepository extends IExecuteTemplateQueryRepository, ConfigCommandRepository {

  /**
  * 保存执行模板
  * @param executeTemplate 执行模板
  * @return 是否成功
  */
  boolean save(ExecuteTemplateBO executeTemplate);

  /**
  * 更新执行模板
  * @param executeTemplate 执行模板
  * @return 是否成功
  */
  boolean updateTemplate(ExecuteTemplateBO executeTemplate);

  /**
  * 删除执行模板
  * @param templateId 模板ID
  * @return 是否成功
  */
  Boolean deleteTemplate(String templateId);

  /**
  * 批量添加模板
  * @param templates 模板列表
  * @return 是否成功
  */
  Boolean batchAddTemplates(List<ExecuteTemplateBO> templates);

}
