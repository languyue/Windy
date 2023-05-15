package com.zj.feature.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.PageSize;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.utils.OrikaUtil;
import com.zj.feature.entity.dto.ExecuteTemplateDTO;
import com.zj.domain.entity.po.feature.ExecuteTemplate;
import com.zj.feature.entity.type.ExecutePointType;
import com.zj.domain.mapper.feeature.ExecuteTemplateMapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class FeatureConfigService extends ServiceImpl<ExecuteTemplateMapper, ExecuteTemplate> {

  @Autowired
  private UniqueIdService uniqueIdService;

  public PageSize<ExecuteTemplateDTO> getFeaturePage(Integer pageNo, Integer size, String name) {
    IPage<ExecuteTemplate> page = new Page<>(pageNo, size);
    LambdaQueryWrapper<ExecuteTemplate> queryWrapper = Wrappers.lambdaQuery(ExecuteTemplate.class)
        .eq(ExecuteTemplate::getTemplateType, ExecutePointType.NORMAL.getType());
    if (!StringUtils.isEmpty(name)) {
      queryWrapper.and(wrapper -> wrapper.like(ExecuteTemplate::getName, name));
    }
    IPage<ExecuteTemplate> templateIPage = page(page, queryWrapper);
    PageSize<ExecuteTemplateDTO> pageSize = new PageSize<>();
    if (CollectionUtils.isEmpty(templateIPage.getRecords())) {
      pageSize.setTotal(0);
      return pageSize;
    }

    List<ExecuteTemplateDTO> templateDTOS = templateIPage.getRecords().stream()
        .map(ExecuteTemplateDTO::toExecuteTemplateDTO).collect(Collectors.toList());
    pageSize.setData(templateDTOS);
    pageSize.setTotal(templateIPage.getTotal());
    return pageSize;
  }

  public ExecuteTemplateDTO getExecuteTemplate(String configId) {
    ExecuteTemplate executeTemplate = getOne(
        Wrappers.<ExecuteTemplate>lambdaQuery().eq(ExecuteTemplate::getTemplateId, configId));
    return ExecuteTemplateDTO.toExecuteTemplateDTO(executeTemplate);
  }

  public String createTemplate(ExecuteTemplateDTO executeTemplateDTO) {
    ExecuteTemplate executeTemplate = OrikaUtil.convert(executeTemplateDTO, ExecuteTemplate.class);
    executeTemplate.setTemplateId(uniqueIdService.getUniqueId());
    executeTemplate.setAuthor("admin");
    executeTemplate.setCreateTime(System.currentTimeMillis());
    executeTemplate.setUpdateTime(System.currentTimeMillis());
    executeTemplate.setParam(JSON.toJSONString(executeTemplateDTO.getParams()));
    boolean result = save(executeTemplate);
    if (result) {
      return executeTemplate.getTemplateId();
    }
    return "";
  }

  public String updateTemplate(ExecuteTemplateDTO executeTemplateDTO) {
    ExecuteTemplate executeTemplate = OrikaUtil.convert(executeTemplateDTO, ExecuteTemplate.class);
    executeTemplate.setAuthor("admin");
    executeTemplate.setUpdateTime(System.currentTimeMillis());
    executeTemplate.setParam(JSON.toJSONString(executeTemplateDTO.getParams()));
    boolean result = update(executeTemplate, Wrappers.lambdaUpdate(ExecuteTemplate.class)
        .eq(ExecuteTemplate::getTemplateId, executeTemplate.getTemplateId()));
    if (result) {
      return executeTemplate.getTemplateId();
    }
    return "";
  }

  public Boolean deleteExecuteTemplate(String templateId) {
    return remove(
        Wrappers.lambdaQuery(ExecuteTemplate.class).eq(ExecuteTemplate::getTemplateId, templateId));
  }

  public List<ExecuteTemplateDTO> getFeatureList() {
    return list().stream().map(ExecuteTemplateDTO::toExecuteTemplateDTO).collect(Collectors.toList());
  }
}
