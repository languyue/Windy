package com.zj.pipeline.service;

import com.zj.contract.git.GitAccessInfo;
import com.zj.common.adapter.invoker.IClientInvoker;
import com.zj.common.adapter.invoker.IMasterApi;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.contract.rpc.ClientCollectDto;
import com.zj.contract.rpc.MasterCollectDto;
import com.zj.domain.entity.bo.pipeline.SystemConfigBO;
import com.zj.domain.entity.vo.DefaultPipelineVo;
import com.zj.domain.entity.vo.ImageRepositoryVo;
import com.zj.domain.entity.vo.GenerateMavenConfigDto;
import com.zj.domain.repository.pipeline.ISystemConfigCommandRepository;
import com.zj.pipeline.entity.dto.SystemMonitorDto;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigService {

  private final ISystemConfigCommandRepository systemConfigRepository;
  private final UniqueIdService uniqueIdService;
  private final IMasterApi masterInvoker;
  private final IClientInvoker clientInvoker;

  public SystemConfigService(ISystemConfigCommandRepository systemConfigRepository,
                             UniqueIdService uniqueIdService, IMasterApi masterInvoker, IClientInvoker clientInvoker) {
    this.systemConfigRepository = systemConfigRepository;
    this.uniqueIdService = uniqueIdService;
    this.masterInvoker = masterInvoker;
    this.clientInvoker = clientInvoker;
  }


  public List<SystemConfigBO> listSystemConfigs() {
    return systemConfigRepository.getAllConfigs();
  }

  public String createSystemConfig(SystemConfigBO systemConfigBO) {
    systemConfigBO.setConfigId(uniqueIdService.getUniqueId());
    return systemConfigRepository.saveConfig(systemConfigBO) ? systemConfigBO.getConfigId()
        : null;
  }

  public Boolean updateSystemConfig(SystemConfigBO systemConfig) {
    return systemConfigRepository.updateConfig(systemConfig);
  }

  public Boolean deleteSystemConfig(String configId) {
    return systemConfigRepository.deleteConfig(configId);
  }

  public SystemConfigBO getSystemConfig(String configId) {
    return systemConfigRepository.getSystemConfig(configId);
  }

  public SystemMonitorDto getSystemMonitor() {
    SystemMonitorDto systemMonitorDto = new SystemMonitorDto();
    List<ClientCollectDto> clientMonitor = clientInvoker.requestClientMonitor();
    systemMonitorDto.setClients(clientMonitor);
    List<MasterCollectDto> masterMonitor = masterInvoker.requestMasterMonitor();
    systemMonitorDto.setMasters(masterMonitor);
    return systemMonitorDto;
  }

  public GitAccessInfo getGitConfig() {
    return systemConfigRepository.getGitAccess();
  }

  public boolean updateGitConfig(GitAccessInfo gitAccessInfo) {
    return systemConfigRepository.updateGitAccess(gitAccessInfo);
  }

  public ImageRepositoryVo getImageRepository() {
    return systemConfigRepository.getImageRepository();
  }

  public boolean updateRepository(ImageRepositoryVo repository) {
    return systemConfigRepository.updateImageRepository(repository);
  }

  public DefaultPipelineVo getDefaultPipeline() {
    return systemConfigRepository.getDefaultPipeline();
  }

  public Boolean updateMavenConfig(GenerateMavenConfigDto mavenConfig) {
    return systemConfigRepository.updateMavenConfig(mavenConfig);
  }

  public GenerateMavenConfigDto getMavenConfig() {
    return systemConfigRepository.getMavenConfig();
  }

  public String createPushSecret() {
    return uniqueIdService.getUniqueId();
  }
}
