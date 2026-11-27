package com.zj.infrastructure.repository.service;

import com.zj.contract.rpc.PageSize;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.contract.json.ContractJson;
import com.zj.contract.pipeline.EnvAccessParams;
import com.zj.contract.pipeline.K8SAccessParams;
import com.zj.contract.pipeline.SSHParams;
import com.zj.domain.entity.bo.service.DeployEnvironmentBO;
import com.zj.domain.entity.enums.EnvType;
import com.zj.domain.entity.enums.SourceStatus;
import com.zj.infrastructure.entity.po.service.DeployEnvironment;
import com.zj.infrastructure.mapper.service.EnvironmentMapper;
import com.zj.domain.repository.service.IEnvironmentCommandRepository;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class EnvironmentRepository extends
    ServiceImpl<EnvironmentMapper, DeployEnvironment> implements IEnvironmentCommandRepository {

  @Override
  public PageSize<DeployEnvironmentBO> getEnvPage(Integer page, Integer size, String name) {
    Page<DeployEnvironment> environmentPage = page(new Page<>(page, size),
        Wrappers.lambdaQuery(DeployEnvironment.class).like(DeployEnvironment::getEnvName, name));

    PageSize<DeployEnvironmentBO> envPage = new PageSize<>();
    envPage.setTotal(environmentPage.getTotal());
    envPage.setData(environmentPage.getRecords().stream()
        .map(this::convertEnvironmentBO).collect(Collectors.toList()));
    return envPage;
  }

  @Override
  public Boolean createEnvironment(DeployEnvironmentBO deployEnvironment) {
    DeployEnvironment environment = convertEnvironment(deployEnvironment);
    long dateNow = System.currentTimeMillis();
    environment.setCreateTime(dateNow);
    environment.setUpdateTime(dateNow);
    return save(environment);
  }

  @Override
  public Boolean updateEnvironment(DeployEnvironmentBO deployEnvironment) {
    DeployEnvironment environment = convertEnvironment(deployEnvironment);
    environment.setUpdateTime(System.currentTimeMillis());
    return update(environment, Wrappers.lambdaQuery(DeployEnvironment.class)
        .eq(DeployEnvironment::getEnvId, environment.getEnvId()));
  }

  @Override
  public Boolean deleteEnvironment(String envId) {
    return remove(
        Wrappers.lambdaQuery(DeployEnvironment.class).eq(DeployEnvironment::getEnvId, envId));
  }

  @Override
  public DeployEnvironmentBO getEnvironment(String envId) {
    DeployEnvironment environment = getOne(
        Wrappers.lambdaQuery(DeployEnvironment.class).eq(DeployEnvironment::getEnvId, envId));
    return convertEnvironmentBO(environment);
  }

  @Override
  public List<DeployEnvironmentBO> getAvailableEnvs() {
    List<DeployEnvironment> environments = list(
        Wrappers.lambdaQuery(DeployEnvironment.class).eq(DeployEnvironment::getEnvStatus,
            SourceStatus.AVAILABLE.getType()));
    return environments.stream().map(this::convertEnvironmentBO).collect(Collectors.toList());
  }

  private DeployEnvironmentBO convertEnvironmentBO(DeployEnvironment environment) {
    if (Objects.isNull(environment)) {
      return null;
    }
    DeployEnvironmentBO environmentBO = OrikaUtil.convert(environment, DeployEnvironmentBO.class);
    environmentBO.setAccessParams(readAccessParams(environment.getEnvParams(), environment.getEnvType()));
    return environmentBO;
  }

  private DeployEnvironment convertEnvironment(DeployEnvironmentBO environmentBO) {
    DeployEnvironment environment = OrikaUtil.convert(environmentBO, DeployEnvironment.class);
    EnvAccessParams accessParams = environmentBO.getAccessParams();
    // 保留 null 语义：MyBatis-Plus 更新时忽略 null 字段，不会被写成字符串 "null"
    environment.setEnvParams(Objects.isNull(accessParams) ? null : ContractJson.write(accessParams));
    return environment;
  }

  /**
   * envParams 是异构列：K8S 环境存集群参数，SSH 环境存主机参数，按 envType 还原成对应对象
   */
  private EnvAccessParams readAccessParams(String envParams, Integer envType) {
    if (Objects.isNull(envParams) || envParams.trim().isEmpty()) {
      return null;
    }
    if (Objects.equals(envType, EnvType.K8S.getType())) {
      return ContractJson.read(envParams, K8SAccessParams.class);
    }
    if (Objects.equals(envType, EnvType.SSH.getType())) {
      return ContractJson.read(envParams, SSHParams.class);
    }
    return null;
  }
}
