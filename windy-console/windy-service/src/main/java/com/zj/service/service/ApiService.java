package com.zj.service.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zj.common.adapter.invoker.IMasterApi;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.common.entity.WindyConstants;
import com.zj.contract.dispatch.DispatchTaskModel;
import com.zj.common.entity.generate.GenerateRecordBO;
import com.zj.contract.service.ApiParamModel;
import com.zj.contract.enums.LogType;
import com.zj.common.enums.Position;
import com.zj.common.enums.TemplateType;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.CommonException;
import com.zj.contract.exception.ErrorCode;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.feature.ExecuteTemplateBO;
import com.zj.domain.entity.bo.service.ServiceApiBO;
import com.zj.domain.entity.bo.service.ServiceGenerateBO;
import com.zj.domain.entity.vo.GenerateMavenConfigDto;
import com.zj.domain.repository.feature.IExecuteTemplateQueryRepository;
import com.zj.domain.repository.pipeline.ISystemConfigQueryRepository;
import com.zj.domain.repository.service.IGenerateRecordQueryRepository;
import com.zj.domain.repository.service.IGenerateCommandRepository;
import com.zj.domain.repository.service.IServiceApiCommandRepository;
import com.zj.plugin.loader.InitData;
import com.zj.plugin.loader.ParamValueType;
import com.zj.plugin.loader.ParameterDefine;
import com.zj.service.entity.*;
import com.zj.service.service.imports.ApiImportFactory;
import com.zj.service.service.imports.IApiImportStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/8/8
 */
@Slf4j
@Service
public class ApiService {

    public static final String HOST_VARIABLE_LABEL = "${host}";
    public static final String HOST_KEY = "host";
    private final UniqueIdService uniqueIdService;
    private final IServiceApiCommandRepository apiRepository;
    private final IMasterApi masterInvoker;
    private final ISystemConfigQueryRepository systemConfigRepository;
    private final IGenerateCommandRepository generateRepository;
    private final IGenerateRecordQueryRepository generateRecordRepository;
    private final ApiImportFactory apiImportFactory;
    private final IExecuteTemplateQueryRepository executeTemplateRepository;

    public ApiService(UniqueIdService uniqueIdService, IServiceApiCommandRepository apiRepository, IMasterApi masterInvoker
            , ISystemConfigQueryRepository systemConfigRepository, IGenerateCommandRepository generateRepository,
                      IGenerateRecordQueryRepository generateRecordRepository, ApiImportFactory apiImportFactory,
                      IExecuteTemplateQueryRepository executeTemplateRepository) {
        this.uniqueIdService = uniqueIdService;
        this.apiRepository = apiRepository;
        this.masterInvoker = masterInvoker;
        this.systemConfigRepository = systemConfigRepository;
        this.generateRepository = generateRepository;
        this.generateRecordRepository = generateRecordRepository;
        this.apiImportFactory = apiImportFactory;
        this.executeTemplateRepository = executeTemplateRepository;
    }

    public ServiceApiBO getServiceApi(String apiId) {
        return apiRepository.getServiceApi(apiId);
    }

    public List<ServiceApiBO> getServiceApis(String serviceId) {
        return apiRepository.getApiByService(serviceId);
    }

    public boolean createServiceApi(ApiModel apiModel) {
        ServiceApiBO serviceApi = OrikaUtil.convert(apiModel, ServiceApiBO.class);
        List<ApiParamModel> requestParams = Optional.ofNullable(apiModel.getRequestParams()).map(params ->
                OrikaUtil.convertList(params, ApiParamModel.class)).orElse(null);
        serviceApi.setRequestParams(requestParams);
        List<ApiParamModel> responseParams = Optional.ofNullable(apiModel.getResponseParams())
                .map(params -> OrikaUtil.convertList(params, ApiParamModel.class)).orElse(null);
        serviceApi.setResponseParams(responseParams);
        serviceApi.setApiId(uniqueIdService.getUniqueId());
        return apiRepository.saveApi(serviceApi);
    }

    public boolean updateServiceApi(ApiModel apiModel) {
        ServiceApiBO serviceApi = OrikaUtil.convert(apiModel, ServiceApiBO.class);
        serviceApi.setRequestParams(OrikaUtil.convertList(apiModel.getRequestParams(), ApiParamModel.class));
        serviceApi.setResponseParams(OrikaUtil.convertList(apiModel.getResponseParams(), ApiParamModel.class));
        return apiRepository.updateApi(serviceApi);
    }

    public boolean deleteServiceApi(String apiId) {
        return apiRepository.deleteApi(apiId);
    }

    public boolean batchDeleteApi(List<String> apiIds) {
        return apiRepository.batchDeleteApi(apiIds);
    }

    public Boolean generateServiceApi(GeneratePackageDto generate) {
        checkMavenConfig();
        checkVersionExist(generate.getServiceId(), generate.getVersion());
        checkGenerateClassName(generate);
        saveOrUpdateParams(generate);

        DispatchTaskModel dispatchTaskModel = new DispatchTaskModel();
        dispatchTaskModel.setSourceIds(Collections.singletonList(generate.getServiceId()));
        dispatchTaskModel.setTriggerIds(generate.getApiIds());
        dispatchTaskModel.setSourceName("generate service=[] package");
        dispatchTaskModel.setType(LogType.GENERATE.getType());
        return masterInvoker.runGenerateTask(dispatchTaskModel);
    }

    /**
     * 检查构建的API是否都已经定义好类型和方法名
     */
    private void checkGenerateClassName(GeneratePackageDto generate) {
        List<ServiceApiBO> serviceApiList = apiRepository.getServiceApiList(generate.getApiIds())
                .stream().filter(ServiceApiBO::isApi).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(serviceApiList)) {
            log.info("service do not have api = {}", generate.getServiceId());
            throw new ApiException(ErrorCode.SERVICE_API_NOT_FIND);
        }
        serviceApiList.forEach(serviceApi -> {
            if (StringUtils.isBlank(serviceApi.getClassName()) || StringUtils.isBlank(serviceApi.getClassMethod())) {
                log.info("service not config class name or method name ={}", serviceApi.getApiId());
                throw new CommonException(ErrorCode.SERVICE_GENERATE_NAME_EMPTY, serviceApi.getApiName());
            }

            if (StringUtils.isBlank(serviceApi.getResultClass())) {
                log.info("api request body name is empty={}", serviceApi.getApiName());
                throw new CommonException(ErrorCode.SERVICE_GENERATE_RESPONSE_NAME_EMPTY, serviceApi.getApiName());
            }

            Optional<ApiParamModel> optional =
                    serviceApi.getRequestParams().stream().filter(param -> Objects.equals(param.getPosition(),
                            Position.Body.name())).findFirst();
            if (optional.isPresent() && StringUtils.isBlank(serviceApi.getBodyClass())) {
                log.info("api request body name is empty={}", serviceApi.getApiName());
                throw new CommonException(ErrorCode.SERVICE_GENERATE_BODY_NAME_EMPTY, serviceApi.getApiName());
            }

            Optional<ApiParamModel> requestOptional =
                    serviceApi.getRequestParams().stream().filter(param -> Objects.equals(param.getPosition(),
                            Position.Body.name()) && StringUtils.isBlank(param.getObjectName())).findFirst();
            if (requestOptional.isPresent()) {
                ApiParamModel requestParam = requestOptional.get();
                log.info("api request body param name is empty api={} param key = {}", serviceApi.getApiName(),
                        requestParam.getParamKey());
                throw new CommonException(ErrorCode.SERVICE_GENERATE_BODY_PARAM_NAME_EMPTY, serviceApi.getApiName());
            }

            Optional<ApiParamModel> responseOptional =
                    serviceApi.getResponseParams().stream().filter(param -> Objects.equals(param.getPosition(),
                            Position.Body.name()) && StringUtils.isBlank(param.getObjectName())).findFirst();
            if (responseOptional.isPresent()) {
                ApiParamModel responseParam = responseOptional.get();
                log.info("api response body param name is empty api={} param key = {}", serviceApi.getApiName(),
                        responseParam.getParamKey());
                throw new CommonException(ErrorCode.SERVICE_GENERATE_RESPONSE_PARAM_NAME_EMPTY,
                        serviceApi.getApiName());
            }
        });
    }

    /**
     * 检查当前服务历史否构建过指定的版本号
     * @param serviceId 服务ID
     * @param version 当前构建的版本号
     */
    private void checkVersionExist(String serviceId, String version) {
        List<GenerateRecordBO> generateRecords = generateRecordRepository.getGenerateRecord(serviceId, version);
        if (CollectionUtils.isNotEmpty(generateRecords)) {
            log.info("generate version exist, can not execute serviceId={} version={}", serviceId, version);
            throw new ApiException(ErrorCode.GENERATE_VERSION_EXIST);
        }
    }

    /**
     * 检查系统是否存在maven配置
     */
    private void checkMavenConfig() {
        GenerateMavenConfigDto mavenConfig = systemConfigRepository.getMavenConfig();
        if (Objects.isNull(mavenConfig) || !mavenConfig.checkConfig()) {
            throw new ApiException(ErrorCode.MAVEN_NOT_CONFIG);
        }
    }

    private void saveOrUpdateParams(GeneratePackageDto generatePackageDto) {
        ServiceGenerateBO serviceGenerateBO = generateRepository.getByService(generatePackageDto.getServiceId());
        if (Objects.isNull(serviceGenerateBO)) {
            ServiceGenerateBO generateBO = OrikaUtil.convert(generatePackageDto, ServiceGenerateBO.class);
            generateBO.setGenerateId(uniqueIdService.getUniqueId());
            generateRepository.create(generateBO);
        } else {
            serviceGenerateBO.setArtifactId(generatePackageDto.getArtifactId());
            serviceGenerateBO.setVersion(generatePackageDto.getVersion());
            serviceGenerateBO.setGroupId(generatePackageDto.getGroupId());
            serviceGenerateBO.setPackageName(generatePackageDto.getPackageName());
            generateRepository.update(serviceGenerateBO);
        }
    }

    public ServiceGenerateBO getGenerateParams(String serviceId) {
        return generateRepository.getByService(serviceId);
    }

    public List<GenerateRecordBO> getLatestGenerateLog(String serviceId) {
        List<GenerateRecordBO> serviceRecords = generateRecordRepository.getServiceRecords(serviceId);
        if (CollectionUtils.isEmpty(serviceRecords)) {
            return Collections.emptyList();
        }
        return serviceRecords.stream().sorted(Comparator.comparing(GenerateRecordBO::getUpdateTime).reversed()).collect(Collectors.toList());
    }

    public ImportApiResult importApiFile(MultipartFile file, String importType, String serviceId) {
        try {
            String fileContent = new String(file.getBytes());
            IApiImportStrategy importStrategy = apiImportFactory.getImportStrategy(importType);
            if (Objects.isNull(importStrategy)) {
                log.info("can not support import type={}", importType);
                return null;
            }
            List<ServiceApiBO> serviceApiList = importStrategy.importContent(serviceId, fileContent);
            return new ImportApiResult(serviceApiList);
        } catch (IOException exception) {
            log.error("import api error importType={}", importType, exception);
        }
        return null;
    }

    public List<ExecuteTemplateDto> apiGenerateTemplate(GenerateTemplate generateTemplate) {
        List<ServiceApiBO> serviceApis = apiRepository.getServiceApiList(generateTemplate.getApiIds());
        if (CollectionUtils.isEmpty(serviceApis)) {
            return Collections.emptyList();
        }

        List<String> templateIds = serviceApis.stream().map(ServiceApiBO::getApiId).collect(Collectors.toList());
        List<String> existTemplateIds = executeTemplateRepository.getTemplateByIds(templateIds)
                .stream().map(ExecuteTemplateBO::getTemplateId).collect(Collectors.toList());
        return serviceApis.stream()
                .filter(serviceApi -> generateTemplate.getCover() || !existTemplateIds.contains(serviceApi.getApiId()))
                .map(serviceApi -> convertApi2Template(serviceApi, generateTemplate.getInvokeType(), generateTemplate.getRelatedId()))
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    private ExecuteTemplateDto convertApi2Template(ServiceApiBO serviceApi, Integer invokeType, String relatedId) {
        if (!serviceApi.isApi()) {
            return null;
        }

        ExecuteTemplateBO executeTemplateBO = new ExecuteTemplateBO();
        executeTemplateBO.setTemplateId(serviceApi.getApiId());
        executeTemplateBO.setName(serviceApi.getApiName());
        executeTemplateBO.setMethod(serviceApi.getMethod());
        executeTemplateBO.setInvokeType(invokeType);
        executeTemplateBO.setTemplateType(TemplateType.NORMAL.getType());
        executeTemplateBO.setDescription(serviceApi.getDescription());
        executeTemplateBO.setOwner(serviceApi.getServiceId());
        executeTemplateBO.setRelatedId(relatedId);
        if (StringUtils.isBlank(serviceApi.getDescription())){
            executeTemplateBO.setDescription(serviceApi.getApiName());
        }

        List<ApiRequestVariable> apiVariables = OrikaUtil.convertList(serviceApi.getRequestParams(),
                ApiRequestVariable.class);
        Map<String, String> header = new HashMap<>();
        List<ParameterDefine> parameterDefines = apiVariables.stream().map(variable -> {
            if (Objects.equals(variable.getPosition(), Position.Header.name())) {
                header.put(variable.getParamKey(), variable.getDefaultValue());
                return null;
            }
            ParameterDefine parameterDefine = getParameterDefine(variable);
            InitData initData = new InitData(variable.getDefaultValue());
            setParamRangeData(variable, initData);
            parameterDefine.setInitData(initData);
            return parameterDefine;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        parameterDefines.add(createHostParam());
        executeTemplateBO.setParameterDefines(parameterDefines);
        executeTemplateBO.setHeaders(header);

        String assembledUrl = assembledApiUrl(serviceApi.getResource(), apiVariables);
        executeTemplateBO.setService(assembledUrl);
        return toExecuteTemplateDTO(executeTemplateBO);
    }

    private ParameterDefine createHostParam() {
        ParameterDefine parameterDefine = new ParameterDefine();
        parameterDefine.setDescription("request domain address");
        parameterDefine.setParamKey(HOST_KEY);
        parameterDefine.setPosition(Position.Path.name());
        parameterDefine.setType(ParamValueType.String.name());
        parameterDefine.setInitData(new InitData(HOST_VARIABLE_LABEL));
        return parameterDefine;
    }

    private void setParamRangeData(ApiRequestVariable apiVariable, InitData initData) {
        if (CollectionUtils.isEmpty(apiVariable.getChildren())) {
            return;
        }

        if (Objects.equals(apiVariable.getType(), ParamValueType.Object.name())) {
            List<ParameterDefine> parameters =
                    apiVariable.getChildren().stream().map(this::getParameterDefine).collect(Collectors.toList());
            initData.setRange(parameters);
            return;
        }


        // 对象数组参数默认创建了一个空child，然后将对象的属性添加到空child的子对象中
        setArrayInitDta(apiVariable, initData);
    }

    private void setArrayInitDta(ApiRequestVariable apiVariable, InitData initData) {
        ApiRequestVariable emptyChild = apiVariable.getChildren().get(0);
        initData.setRangeType(emptyChild.getType());
        if (Objects.equals(emptyChild.getType(), ParamValueType.Object.name())) {
            List<ParameterDefine> rangeList = emptyChild.getChildren().stream().map(childVariable -> {
                InitData init = new InitData(childVariable.getDefaultValue());
                if (Objects.equals(childVariable.getType(), "Array") && CollectionUtils.isNotEmpty(childVariable.getChildren())) {
                    setArrayInitDta(childVariable, init);
                }
                ParameterDefine param = getParameterDefine(childVariable);
                param.setInitData(init);
                return param;
            }).collect(Collectors.toList());
            initData.setRange(rangeList);
        }
    }

    private ParameterDefine getParameterDefine(ApiRequestVariable variable) {
        ParameterDefine parameterDefine = new ParameterDefine();
        parameterDefine.setParamKey(variable.getParamKey());
        parameterDefine.setType(variable.getType());
        parameterDefine.setDescription(variable.getDescription());
        parameterDefine.setPosition(variable.getPosition());
        return parameterDefine;
    }

    /**
     * 将uri路径参数添加进去
     *
     * @param uri          原始rest请求的api
     * @param apiVariables 请求的参数
     */
    private String assembledApiUrl(String uri, List<ApiRequestVariable> apiVariables) {
        StringBuilder uriBuilder = new StringBuilder(uri);
        for (ApiRequestVariable variable : apiVariables) {
            if (Objects.equals(variable.getPosition(), Position.Query.name())) {
                String paramKey = variable.getParamKey();
                uriBuilder.append(uriBuilder.indexOf("?") >= 0 ? "&" : "?").append(String.format("%s=${%s}", paramKey
                        , paramKey));
            }
            if (Objects.equals(variable.getPosition(), Position.Path.name())) {
                uriBuilder = new StringBuilder(uriBuilder.toString().replace("{" + variable.getParamKey() + "}",
                        WindyConstants.VARIABLE_CHAR + "{" + variable.getParamKey() + "}"));
            }
        }
        uri = uriBuilder.toString();
        return HOST_VARIABLE_LABEL + uri;
    }

    public ExecuteTemplateDto toExecuteTemplateDTO(ExecuteTemplateBO executeTemplate) {
        ExecuteTemplateDto templateVo = OrikaUtil.convert(executeTemplate, ExecuteTemplateDto.class);
        templateVo.setParams(executeTemplate.getParameterDefines());
        templateVo.setHeaders(executeTemplate.getHeaders());
        return templateVo;
    }
}
