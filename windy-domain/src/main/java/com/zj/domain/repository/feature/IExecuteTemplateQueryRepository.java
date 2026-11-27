package com.zj.domain.repository.feature;

import com.zj.contract.rpc.PageSize;
import com.zj.domain.entity.bo.feature.ExecuteTemplateBO;
import java.util.List;

/**
* @author guyuelan
* @since 2023/5/18
*/
public interface IExecuteTemplateQueryRepository {

  /**
  * 获取执行模板
  * @param templateId 模板ID
  * @return 模板
  */
  ExecuteTemplateBO getExecuteTemplate(String templateId);

  /**
  * 分页获取服务下执行模板列表
  * @param serviceId 服务ID
  * @param pageNo 页码
  * @param size 每页数量
  * @param name 模板名称
  * @return 模板列表
  */
  PageSize<ExecuteTemplateBO> getPage(String serviceId, Integer pageNo, Integer size, String name);

  /**
  * 获取服务下的模板
  * @param serviceId 服务ID
  * @return 模板列表
  */
  List<ExecuteTemplateBO> getServiceTemplates(String serviceId);

  /**
  * 获取所有模板
  * @return 模板列表
  */
  List<ExecuteTemplateBO> getAllTemplates();

  /**
  * 根据模版类型获取模板
  * @param templateTypes 模版类型
  * @return 模板
  */
  List<ExecuteTemplateBO> getTemplatesByType(List<Integer> templateTypes);

  /**
  * 获取基础工具模板
  * @return 模板列表
  */
  List<ExecuteTemplateBO> getToolTemplates();

  /**
  * 根据模板ID列表获取模板
  * @param templateIds 模板ID列表
  * @return 模板列表
  */
  List<ExecuteTemplateBO> getTemplateByIds(List<String> templateIds);

  /**
  * 根据服务ID列表获取模板
  * @param serviceIds 服务ID列表
  * @return 模板列表
  */
  List<ExecuteTemplateBO> getTemplatesByServiceIds(List<String> serviceIds);

  /**
  * 查询插件关联的模版列表
  * @param pluginId 插件ID
  * @return 模版列表
  */
  List<ExecuteTemplateBO> getPluginTemplates(String pluginId);

}
