package com.zj.master.dispatch.generate;

import com.zj.contract.feature.exec.GenerateParam;
import com.zj.contract.service.ApiModel;
import com.zj.common.adapter.invoker.IClientInvoker;
import com.zj.contract.dispatch.DispatchTaskModel;
import com.zj.contract.pipeline.ServiceConfig;
import com.zj.contract.service.ApiParamModel;
import com.zj.common.enums.DispatchType;
import com.zj.contract.enums.LogType;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.log.DispatchLogBO;
import com.zj.domain.entity.bo.service.MicroserviceBO;
import com.zj.domain.entity.bo.service.ServiceApiBO;
import com.zj.domain.entity.bo.service.ServiceGenerateBO;
import com.zj.domain.entity.vo.GenerateMavenConfigDto;
import com.zj.domain.repository.pipeline.ISystemConfigQueryRepository;
import com.zj.domain.repository.service.IGenerateQueryRepository;
import com.zj.domain.repository.service.IMicroServiceQueryRepository;
import com.zj.domain.repository.service.IServiceApiQueryRepository;
import com.zj.master.dispatch.IDispatchExecutor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class GenerateDispatcher implements IDispatchExecutor {

  private final IGenerateQueryRepository generateRepository;
  private final IServiceApiQueryRepository serviceApiRepository;
  private final ISystemConfigQueryRepository systemConfigRepository;
  private final IMicroServiceQueryRepository serviceRepository;
  private final IClientInvoker clientInvoker;

  public GenerateDispatcher(IGenerateQueryRepository generateRepository,
                            IServiceApiQueryRepository serviceApiRepository, ISystemConfigQueryRepository systemConfigRepository,
                            IMicroServiceQueryRepository serviceRepository, IClientInvoker clientInvoker) {
    this.generateRepository = generateRepository;
    this.serviceApiRepository = serviceApiRepository;
    this.systemConfigRepository = systemConfigRepository;
    this.serviceRepository = serviceRepository;
    this.clientInvoker = clientInvoker;
  }

  @Override
  public LogType type() {
    return LogType.GENERATE;
  }

  @Override
  public boolean isExistInJvm(DispatchLogBO taskLog) {
    return false;
  }

  @Override
  public Boolean dispatch(DispatchTaskModel task, String logId) {
    String serviceId = task.firstSourceId();
    ServiceGenerateBO serviceGenerate = generateRepository.getByService(serviceId);
    if (Objects.isNull(serviceGenerate)) {
      return false;
    }

    GenerateParam generateParam = OrikaUtil.convert(serviceGenerate, GenerateParam.class);
    generateParam.setDispatchType(DispatchType.GENERATE.name());
    GenerateMavenConfigDto mavenConfig = systemConfigRepository.getMavenConfig();
    generateParam.setMavenUser(mavenConfig.getUserName());
    generateParam.setMavenPwd(mavenConfig.getPassword());
    generateParam.setMavenRepository(mavenConfig.getMavenUrl());
    generateParam.setServiceId(serviceId);

    List<String> apiIdList = task.getTriggerIds();
    if (CollectionUtils.isEmpty(apiIdList)) {
      return false;
    }
    List<ServiceApiBO> apiList = serviceApiRepository.getServiceApiList(apiIdList);
    if (CollectionUtils.isEmpty(apiList)) {
      return false;
    }
    List<ApiModel> models = apiList.stream().filter(ServiceApiBO::isApi).map(api -> {
      ApiModel apiModel = OrikaUtil.convert(api, ApiModel.class);
      apiModel.setRequestParamList(OrikaUtil.convertList(api.getRequestParams(), ApiParamModel.class));
      apiModel.setResponseParamList(OrikaUtil.convertList(api.getResponseParams(), ApiParamModel.class));
      return apiModel;
    }).collect(Collectors.toList());
    generateParam.setApiList(models);

    MicroserviceBO service = serviceRepository.queryServiceDetail(serviceId);
    ServiceConfig serviceConfig = service.getServiceConfig();
    Optional.ofNullable(serviceConfig.getServiceContext()).ifPresent(context ->
            generateParam.setBuildPath(context.getBuildVersion()));
    generateParam.setService(service.getServiceName());
    return clientInvoker.runGenerateTask(generateParam);
  }

  @Override
  public boolean resume(DispatchLogBO taskLog) {
    return false;
  }

  @Override
  public Integer getExecuteCount() {
    return 1;
  }
}
