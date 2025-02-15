package com.zj.feature.service;

import com.zj.common.enums.CompareType;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.domain.entity.bo.feature.ExecutePointBO;
import com.zj.domain.entity.bo.feature.ExecuteTemplateBO;
import com.zj.domain.entity.bo.service.MicroserviceBO;
import com.zj.domain.repository.feature.IExecutePointRepository;
import com.zj.domain.repository.feature.IExecuteTemplateRepository;
import com.zj.domain.repository.service.IMicroServiceRepository;
import com.zj.feature.entity.CompareOperator;
import com.zj.feature.entity.ExecutePointTemplate;
import com.zj.feature.entity.ExecutePointDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExecutePointService {

    private final UniqueIdService uniqueIdService;
    private final IExecutePointRepository executePointRepository;
    private final IExecuteTemplateRepository templateRepository;
    private final IMicroServiceRepository microServiceRepository;

    public ExecutePointService(UniqueIdService uniqueIdService, IExecutePointRepository executePointRepository, IExecuteTemplateRepository templateRepository, IMicroServiceRepository microServiceRepository) {
        this.uniqueIdService = uniqueIdService;
        this.executePointRepository = executePointRepository;
        this.templateRepository = templateRepository;
        this.microServiceRepository = microServiceRepository;
    }

    public boolean updateByPointId(ExecutePointBO executePoint) {
        return executePointRepository.updateExecutePoint(executePoint);
    }

    public boolean deleteByExecutePointId(String pointId) {
        return executePointRepository.deleteExecutePoint(pointId);
    }

    public boolean deleteByFeatureId(String featureId) {
        return executePointRepository.deleteByFeatureId(featureId);
    }

    private ExecutePointBO getExecutePointById(String executePointId) {
        return executePointRepository.getExecutePoint(executePointId);
    }

    public List<ExecutePointBO> getExecutePointByFeatureIds(List<String> featureIds) {
        return executePointRepository.getPointsByFeatureIds(featureIds);
    }

    public String createExecutePoint(ExecutePointDto executePointDto) {
        ExecutePointBO executePoint = toExecutePoint(executePointDto);
        executePoint.setPointId(uniqueIdService.getUniqueId());
        boolean result = executePointRepository.saveExecutePoint(executePoint);

        log.info("create feature detail result = {}", result);
        return executePoint.getPointId();
    }

    public ExecutePointBO toExecutePoint(ExecutePointDto dto) {
        ExecutePointBO point = new ExecutePointBO();
        point.setFeatureId(dto.getFeatureId());
        point.setPointId(dto.getPointId());
        point.setDescription(dto.getDescription());
        point.setSortOrder(dto.getSortOrder());
        point.setTemplateId(dto.getTemplateId());
        point.setTestStage(dto.getTestStage());
        point.setExecuteType(dto.getExecuteType());
        point.setCompareDefines(dto.getCompareDefine());
        point.setVariableDefines(dto.getVariableDefine());
        point.setExecutorUnit(dto.getExecutorUnit());
        return point;
    }

    public String updateExecutePoint(ExecutePointDto executePointDto) {
        ExecutePointBO executePoint = getExecutePointById(executePointDto.getPointId());
        if (Objects.isNull(executePoint)) {
            return null;
        }

        executePoint.setTestStage(executePointDto.getTestStage());
        executePoint.setDescription(executePointDto.getDescription());
        executePoint.setSortOrder(executePointDto.getSortOrder());
        executePoint.setExecuteType(executePointDto.getExecuteType());
        executePoint.setExecutorUnit(executePointDto.getExecutorUnit());
        executePoint.setCompareDefines(executePointDto.getCompareDefine());
        executePoint.setVariableDefines(executePointDto.getVariableDefine());
        executePoint.setUpdateTime(System.currentTimeMillis());
        boolean result = updateByPointId(executePoint);

        log.info("update feature detail result = {}", result);
        return executePoint.getPointId();
    }

    public void batchAddTestFeature(List<ExecutePointDto> executePoints) {
        if (CollectionUtils.isEmpty(executePoints)) {
            log.warn("batch add test feature is empty list");
            return;
        }

        executePoints.forEach(executePointDTO -> {
            ExecutePointBO executePoint = getExecutePointById(executePointDTO.getPointId());
            if (Objects.isNull(executePoint)) {
                createExecutePoint(executePointDTO);
            } else {
                updateExecutePoint(executePointDTO);
            }
        });
    }

    public List<CompareOperator> queryExecutePointOperators() {
        return Arrays.stream(CompareType.values()).map(type ->{
            CompareOperator compareOperator = new CompareOperator();
            compareOperator.setOperator(type.getOperator());
            compareOperator.setDescription(type.getDesc());
            return compareOperator;
        }).collect(Collectors.toList());
    }

    public boolean saveBatch(List<ExecutePointBO> newExecutePoints) {
        return executePointRepository.batchSavePoints(newExecutePoints);
    }

    public ExecutePointDto getExecutePoint(String executePointId) {
        ExecutePointBO executePoint = getExecutePointById(executePointId);
        return ExecutePointDto.toExecutePointDTO(executePoint);
    }

    public List<ExecutePointDto> getExecutePointsByFeatureId(String featureId) {
        List<ExecutePointBO> executePoints = executePointRepository.getExecutePointByFeatureId(featureId);
        return executePoints.stream().map(ExecutePointDto::toExecutePointDTO)
                .sorted(Comparator.comparing(ExecutePointDto::getSortOrder)).collect(Collectors.toList());
    }

    public ExecutePointTemplate queryPointTemplate(String executePointId) {
        ExecutePointBO executePoint = executePointRepository.getExecutePoint(executePointId);
        if (Objects.isNull(executePoint)) {
            log.info("can not find execute point = {}", executePointId);
            throw new ApiException(ErrorCode.EXECUTE_POINT_NOT_FIND);
        }

        ExecuteTemplateBO executeTemplate = templateRepository.getExecuteTemplate(executePoint.getTemplateId());
        if (Objects.isNull(executeTemplate)) {
            log.info("can not find template = {}", executePoint.getTemplateId());
            throw new ApiException(ErrorCode.TEMPLATE_NOT_FIND);
        }

        ExecutePointTemplate executePointTemplate = new ExecutePointTemplate();
        executePointTemplate.setInvokeType(executeTemplate.getInvokeType());
        executePointTemplate.setRequest(executeTemplate.getService());
        executePointTemplate.setMethod(executeTemplate.getMethod());
        executePointTemplate.setDescription(executeTemplate.getDescription());
        MicroserviceBO microserviceBO = microServiceRepository.queryServiceDetail(executeTemplate.getOwner());
        if (Objects.nonNull(microserviceBO)) {
            executePointTemplate.setService(microserviceBO.getServiceName());
        }
        return executePointTemplate;
    }
}
