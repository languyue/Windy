package com.zj.service.service.imports.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.zj.contract.json.ContractJson;
import com.zj.contract.service.ApiParamModel;
import com.zj.common.enums.ApiType;
import com.zj.common.enums.Position;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.service.ServiceApiBO;
import com.zj.domain.repository.service.IServiceApiCommandRepository;
import com.zj.plugin.loader.ParamValueType;
import com.zj.service.entity.ApiRequestVariable;
import com.zj.service.entity.PostmanImport;
import com.zj.service.service.imports.IApiImportStrategy;
import com.zj.service.service.imports.ImportType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PostmanImportStrategy implements IApiImportStrategy {
    private final UniqueIdService uniqueIdService;
    private final IServiceApiCommandRepository serviceApiRepository;
    private final String variableString = "\\{\\{([^\\}]+)\\}\\}";

    public PostmanImportStrategy(UniqueIdService uniqueIdService, IServiceApiCommandRepository serviceApiRepository) {
        this.uniqueIdService = uniqueIdService;
        this.serviceApiRepository = serviceApiRepository;
    }

    @Override
    public String importType() {
        return ImportType.Postman.name();
    }

    @Override
    @Transactional
    public List<ServiceApiBO> importContent(String serviceId, String fileContent) {
        PostmanImport postmanImport = ContractJson.read(fileContent, PostmanImport.class);
        if (Objects.isNull(postmanImport)) {
            return Collections.emptyList();
        }

        ServiceApiBO dirApi = createApiDir(serviceId, postmanImport);
        List<ServiceApiBO> serviceApiList = postmanImport.getItem().stream().map(postmanApi -> {
            PostmanImport.PostmanApiRequest request = postmanApi.getRequest();
            ServiceApiBO serviceApi = new ServiceApiBO();
            serviceApi.setApiId(uniqueIdService.getUniqueId());
            serviceApi.setApiType(ApiType.API.getType());
            serviceApi.setApiName(postmanApi.getName());
            serviceApi.setDescription(postmanApi.getName());
            serviceApi.setMethod(request.getMethod());
            serviceApi.setServiceId(serviceId);
            serviceApi.setParentId(dirApi.getApiId());
            serviceApi.setType("http");

            Optional.ofNullable(request.getUrl()).ifPresent(url -> {
                String path = String.join("/", request.getUrl().getPath());
                serviceApi.setResource("/" + path);
            });


            List<ApiRequestVariable> variableList = getRequestParams(request);
            serviceApi.setRequestParams(OrikaUtil.convertList(variableList, ApiParamModel.class));
            Optional.ofNullable(request.getHeader()).ifPresent(headers -> {
                Map<String, String> headerMap =
                        headers.stream().collect(Collectors.toMap(PostmanImport.PostmanApiHeader::getKey,
                                PostmanImport.PostmanApiHeader::getValue));
                serviceApi.setHeader(headerMap);
            });
            return serviceApi;
        }).collect(Collectors.toList());
        boolean saveBatch = serviceApiRepository.saveBatch(serviceApiList);
        log.info("batch save postman api result = {}", saveBatch);
        return serviceApiList;
    }

    private ServiceApiBO createApiDir(String serviceId, PostmanImport postmanImport) {
        String name = postmanImport.getInfo().getName();
        ServiceApiBO serviceApiBO = new ServiceApiBO();
        serviceApiBO.setApiId(uniqueIdService.getUniqueId());
        serviceApiBO.setApiName(name);
        serviceApiBO.setApiType(ApiType.DIR.getType());
        serviceApiBO.setServiceId(serviceId);
        serviceApiRepository.saveApi(serviceApiBO);
        return serviceApiBO;
    }

    private List<ApiRequestVariable> getRequestParams(PostmanImport.PostmanApiRequest request) {
        PostmanImport.PostmanApiUrl postmanApiUrl = request.getUrl();
        List<ApiRequestVariable> variableList = new ArrayList<>();
        if (Objects.nonNull(postmanApiUrl)){
            List<ApiRequestVariable> pathVariableList = getPathVariableList(postmanApiUrl.getPath());
            variableList.addAll(pathVariableList);

            if (CollectionUtils.isNotEmpty(postmanApiUrl.getQuery())) {
                List<ApiRequestVariable> queryVariableList = getQueryVariableList(request);
                variableList.addAll(queryVariableList);
            }
        }

        List<ApiRequestVariable> bodyVariableList = getBodyVariableList(request);
        variableList.addAll(bodyVariableList);
        return variableList;
    }

    private List<ApiRequestVariable> getQueryVariableList(PostmanImport.PostmanApiRequest request) {
        return request.getUrl().getQuery().stream().map(query -> {
            ApiRequestVariable apiRequestVariable = new ApiRequestVariable();
            apiRequestVariable.setParamKey(query.getKey());
            apiRequestVariable.setType(ParamValueType.String.name());
            apiRequestVariable.setRequired(true);
            apiRequestVariable.setPosition(Position.Query.name());


            Pattern pattern = Pattern.compile(variableString);
            Matcher matcher = pattern.matcher(query.getValue());
            if (!matcher.find()) {
                return apiRequestVariable;
            }
            apiRequestVariable.setDefaultValue(query.getValue());
            return apiRequestVariable;
        }).collect(Collectors.toList());
    }

    private List<ApiRequestVariable> getBodyVariableList(PostmanImport.PostmanApiRequest request) {
        if (Objects.isNull(request.getBody())) {
            return Collections.emptyList();
        }

        JsonNode jsonObject = parseJsonString(request);
        if (Objects.isNull(jsonObject)) {
            return Collections.emptyList();
        }

        return fieldEntries(jsonObject).stream().map(e -> {
            ApiRequestVariable apiRequestVariable = new ApiRequestVariable();
            apiRequestVariable.setParamKey(e.getKey());
            apiRequestVariable.setPosition(Position.Body.name());
            apiRequestVariable.setType(convertVariableType(e.getValue()));
            return apiRequestVariable;
        }).collect(Collectors.toList());
    }

    private JsonNode parseJsonString(PostmanImport.PostmanApiRequest request) {
        try {
            return ContractJson.readTree(request.getBody().getRaw());
        } catch (Exception e) {
            log.info("convert json error", e);
        }
        return null;
    }

    private List<Map.Entry<String, JsonNode>> fieldEntries(JsonNode node) {
        List<Map.Entry<String, JsonNode>> entries = new ArrayList<>();
        if (Objects.isNull(node) || !node.isObject()) {
            return entries;
        }
        Iterator<Map.Entry<String, JsonNode>> iterator = node.fields();
        while (iterator.hasNext()) {
            entries.add(iterator.next());
        }
        return entries;
    }

    private String convertVariableType(JsonNode value) {
        if (value.isTextual()) {
            return ParamValueType.String.name();
        } else if (value.isInt()) {
            return ParamValueType.Integer.name();
        } else if (value.isBoolean()) {
            return ParamValueType.Boolean.name();
        } else if (value.isLong()) {
            return ParamValueType.Long.name();
        } else if (value.isDouble() || value.isFloat()) {
            return ParamValueType.Float.name();
        } else if (value.isArray()) {
            return ParamValueType.Array.name();
        } else if (value.isObject()) {
            return ParamValueType.Object.name();
        } else {
            return null;
        }
    }

    private List<ApiRequestVariable> getPathVariableList(List<String> variableList) {
        return variableList.stream().map(string -> {
            Pattern pattern = Pattern.compile(variableString);
            Matcher matcher = pattern.matcher(string);
            if (!matcher.find()) {
                return null;
            }
            String paramName = matcher.group(1);
            ApiRequestVariable apiRequestVariable = new ApiRequestVariable();
            apiRequestVariable.setParamKey(paramName);
            apiRequestVariable.setType(ParamValueType.String.name());
            apiRequestVariable.setRequired(true);
            apiRequestVariable.setPosition(Position.Path.name());
            return apiRequestVariable;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
