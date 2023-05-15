package com.zj.pipeline.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.netflix.discovery.shared.Applications;
import com.zj.common.ResponseMeta;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.pipeline.PipelineActionDto;
import com.zj.domain.entity.dto.pipeline.PipelineDTO;
import com.zj.domain.entity.dto.pipeline.PipelineNodeDTO;
import com.zj.domain.entity.dto.pipeline.PipelineStageDTO;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import com.zj.pipeline.entity.enums.PipelineStatus;
import com.zj.domain.entity.po.pipeline.NodeRecord;
import com.zj.domain.entity.po.pipeline.Pipeline;
import com.zj.domain.entity.po.pipeline.PipelineNode;
import com.zj.domain.entity.po.pipeline.PipelineStage;
import com.zj.pipeline.entity.vo.ActionDetail;
import com.zj.pipeline.entity.vo.ConfigDetail;
import com.zj.pipeline.executer.Invoker.builder.RefreshContextBuilder;
import com.zj.pipeline.executer.Invoker.builder.RequestContextBuilder;
import com.zj.pipeline.executer.PipelineExecutor;
import com.zj.pipeline.executer.notify.PipelineEventFactory;
import com.zj.pipeline.executer.vo.ExecuteParam;
import com.zj.pipeline.executer.vo.NodeConfig;
import com.zj.pipeline.executer.vo.PipelineStatusEvent;
import com.zj.pipeline.executer.vo.RefreshContext;
import com.zj.pipeline.executer.vo.RequestContext;
import com.zj.pipeline.executer.vo.Stage;
import com.zj.pipeline.executer.vo.TaskNode;
import com.zj.domain.mapper.pipeline.PipelineMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * @author guyuelan
 * @since 2021/9/28
 */
@Slf4j
@Service
public class PipelineService extends ServiceImpl<PipelineMapper, Pipeline> {

  @Autowired
  private PipelineNodeService pipelineNodeService;

  @Autowired
  private PipelineStageService pipelineStageService;

  @Autowired
  private PipelineExecutor pipelineExecutor;

  @Autowired
  private PipelineActionService pipelineActionService;

  @Autowired
  private NodeRecordService nodeRecordService;

  @Autowired
  private UniqueIdService uniqueIdService;

  @Autowired
  private IPipelineRepository pipelineRepository;

  @Transactional
  public boolean updatePipeline(String service, String pipelineId, PipelineDTO pipelineDTO) {
    Assert.notEmpty(service, "service can not be empty");
    PipelineDTO oldPipeline = getPipelineDetail(pipelineId);
    if (Objects.isNull(oldPipeline)) {
      throw new ApiException(ErrorCode.NOT_FOUND_PIPELINE);
    }

    boolean result = pipelineRepository.updatePipeline(pipelineDTO);
    if (!result) {
      throw new ApiException(ErrorCode.UPDATE_PIPELINE_ERROR);
    }

    List<PipelineStageDTO> stageList = pipelineDTO.getStageList();
    List<PipelineStageDTO> temp = JSON.parseArray(JSON.toJSONString(stageList),
        PipelineStageDTO.class);
    addOrUpdateNode(pipelineId, stageList);

    //删除不存在的节点
    deleteNotExistStageAndNodes(oldPipeline, temp);
    return true;
  }

  private void deleteNotExistStageAndNodes(PipelineDTO oldPipeline,
      List<PipelineStageDTO> stageList) {
    List<String> nodeIds = stageList.stream().map(PipelineStageDTO::getNodes)
        .flatMap(Collection::stream).filter(Objects::nonNull).map(PipelineNodeDTO::getNodeId)
        .collect(Collectors.toList());

    List<String> notExistNodes = oldPipeline.getStageList().stream().map(PipelineStageDTO::getNodes)
        .filter(CollectionUtils::isNotEmpty).flatMap(Collection::stream)
        .map(PipelineNodeDTO::getNodeId).filter(nodeId -> !nodeIds.contains(nodeId))
        .collect(Collectors.toList());

    //如果node节点未变更则直接退出
    if (CollectionUtils.isNotEmpty(notExistNodes)) {
      pipelineNodeService.remove(
          Wrappers.lambdaQuery(PipelineNode.class).in(PipelineNode::getNodeId, notExistNodes));
    }

    List<String> newStageIds = stageList.stream().map(PipelineStageDTO::getStageId)
        .collect(Collectors.toList());
    List<String> notExistStages = oldPipeline.getStageList().stream()
        .map(PipelineStageDTO::getStageId).filter(stageId -> !newStageIds.contains(stageId))
        .collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(notExistStages)) {
      pipelineStageService.remove(
          Wrappers.lambdaQuery(PipelineStage.class).in(PipelineStage::getStageId, notExistStages));
    }
  }

  private void addOrUpdateNode(String pipelineId, List<PipelineStageDTO> stageList) {
    if (CollectionUtils.isEmpty(stageList)) {
      return;
    }

    stageList.forEach(stageDto -> {
      PipelineStage stage = pipelineStageService.getOne(Wrappers.lambdaQuery(PipelineStage.class)
          .eq(PipelineStage::getStageId, stageDto.getStageId()));
      if (Objects.isNull(stage)) {
        createNewStage(pipelineId, stageDto);
        return;
      }

      //修改stage节点
      Long currentTime = System.currentTimeMillis();
      PipelineStage pipelineStage = OrikaUtil.convert(stageDto, PipelineStage.class);
      pipelineStage.setUpdateTime(currentTime);
      pipelineStageService.update(pipelineStage, Wrappers.lambdaUpdate(PipelineStage.class)
          .eq(PipelineStage::getStageId, pipelineStage.getStageId()));

      //修改node节点
      List<PipelineNodeDTO> stageDtoNodes = stageDto.getNodes();
      if (CollectionUtils.isNotEmpty(stageDtoNodes)) {
        stageDtoNodes.forEach(dto -> {
          PipelineNode pipelineNode = OrikaUtil.convert(dto, PipelineNode.class);
          pipelineNode.setUpdateTime(currentTime);
          pipelineNodeService.update(pipelineNode, Wrappers.lambdaUpdate(PipelineNode.class)
              .eq(PipelineNode::getNodeId, pipelineNode.getNodeId()));
        });
      }
    });
  }


  public List<PipelineDTO> listPipelines(String serviceId) {
    return pipelineRepository.listPipelines(serviceId);
  }

  @Transactional
  public Boolean deletePipeline(String service, String pipelineId) {
    pipelineStageService.remove(
        Wrappers.lambdaQuery(PipelineStage.class).eq(PipelineStage::getPipelineId, pipelineId));
    pipelineNodeService.remove(
        Wrappers.lambdaQuery(PipelineNode.class).eq(PipelineNode::getPipelineId, pipelineId));
    return pipelineRepository.deletePipeline(pipelineId);
  }

  @Transactional
  public String createPipeline(PipelineDTO pipelineDTO) {
    if (Objects.isNull(pipelineDTO)) {
      return "";
    }

    String pipelineId = uniqueIdService.getUniqueId();
    pipelineDTO.setPipelineId(pipelineId);
    pipelineDTO.setPipelineStatus(PipelineStatus.NORMAL.getType());
    boolean result = pipelineRepository.createPipeline(pipelineDTO);
    if (!result) {
      throw new ApiException(ErrorCode.CREATE_PIPELINE);
    }

    pipelineDTO.getStageList().forEach(stageDto -> createNewStage(pipelineId, stageDto));
    return pipelineId;
  }

  private void createNewStage(String pipelineId, PipelineStageDTO stageDto) {
    Long currentTime = System.currentTimeMillis();
    String stageId = uniqueIdService.getUniqueId();
    PipelineStage pipelineStage = new PipelineStage();
    pipelineStage.setPipelineId(pipelineId);
    pipelineStage.setStageName(stageDto.getStageName());
    pipelineStage.setStageId(stageId);
    pipelineStage.setConfigId(stageDto.getConfigId());
    pipelineStage.setType(stageDto.getType());
    pipelineStage.setCreateTime(currentTime);
    pipelineStage.setUpdateTime(currentTime);
    pipelineStageService.save(pipelineStage);

    stageDto.getNodes().forEach(nodeDto -> {
      PipelineNode pipelineNode = new PipelineNode();
      pipelineNode.setNodeId(uniqueIdService.getUniqueId());
      pipelineNode.setPipelineId(pipelineId);
      pipelineNode.setStageId(stageId);
      pipelineNode.setType(nodeDto.getType());
      pipelineNode.setNodeName(nodeDto.getNodeName());
      pipelineNode.setCreateTime(currentTime);
      pipelineNode.setUpdateTime(currentTime);
      pipelineNode.setConfigDetail(nodeDto.getConfigDetail());
      pipelineNodeService.save(pipelineNode);
    });
  }

  public PipelineDTO getPipeline(String pipelineId) {
    return pipelineRepository.getPipeline(pipelineId);
  }

  @Autowired
  private RestTemplate restTemplate;


  @Autowired
  private DiscoveryClient discoveryClient;
  public String execute(String pipelineId) {
    PipelineDTO pipeline = getPipeline(pipelineId);
    if (true) {
      List<String> services = discoveryClient.getServices();
      log.info("get service list ={}", JSON.toJSONString(services));
      String url = "http://WindyMaster/v1/devops/dispatch/task";
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("sourceId", pipelineId);
      jsonObject.put("sourceName", pipeline.getPipelineName());
      jsonObject.put("type", 1);
      ResponseEntity<ResponseMeta> responseEntity = restTemplate.postForEntity(url, jsonObject,
          ResponseMeta.class);
      log.info("get test result code= {} result={}", responseEntity.getStatusCode(),
          responseEntity.getBody());
      return "11";
    }

    if (Objects.isNull(pipeline)) {
      return null;
    }

    List<PipelineStage> pipelineStages = pipelineStageService.list(
        Wrappers.lambdaQuery(PipelineStage.class).eq(PipelineStage::getPipelineId, pipelineId)
            .orderByAsc(PipelineStage::getType));
    if (CollectionUtils.isEmpty(pipelineStages)) {
      return null;
    }

    ExecuteParam executeParam = new ExecuteParam();
    executeParam.setPipelineId(pipelineId);
    executeParam.setName(pipeline.getPipelineName());

    List<Stage> stageList = pipelineStages.stream().map(this::assembleStageData)
        .collect(Collectors.toList());
    //去除首尾开始与结束节点
    stageList.remove(0);
    stageList.remove(stageList.size() - 1);
    executeParam.setStages(stageList);
    return pipelineExecutor.execute(executeParam);
  }

  private Stage assembleStageData(PipelineStage pipelineStage) {
    Stage stage = new Stage();
    stage.setStageId(pipelineStage.getStageId());
    stage.setStageName(pipelineStage.getStageName());

    List<PipelineNode> pipelineNodes = pipelineNodeService.list(
        Wrappers.lambdaQuery(PipelineNode.class)
            .eq(PipelineNode::getStageId, pipelineStage.getStageId())
            .orderByAsc(PipelineNode::getType));
    if (CollectionUtils.isEmpty(pipelineNodes)) {
      return stage;
    }

    List<TaskNode> taskNodes = pipelineNodes.stream().map(this::buildTaskNode)
        .collect(Collectors.toList());
    stage.setNodeList(taskNodes);
    return stage;
  }

  private TaskNode buildTaskNode(PipelineNode pipelineNode) {
    TaskNode taskNode = new TaskNode();
    taskNode.setNodeId(pipelineNode.getNodeId());
    taskNode.setName(pipelineNode.getNodeName());
    taskNode.setExecuteTime(System.currentTimeMillis());

    ConfigDetail configDetail = JSON.parseObject(pipelineNode.getConfigDetail(),
        ConfigDetail.class);
    PipelineActionDto action = pipelineActionService.getAction(configDetail.getActionId());
    taskNode.setExecuteType(action.getExecuteType());

    ActionDetail actionDetail = new ActionDetail(configDetail, action);
    RequestContext requestContext = RequestContextBuilder.createContext(actionDetail);
    taskNode.setRequestContext(requestContext);

    RefreshContext refreshContext = RefreshContextBuilder.createContext(actionDetail);
    taskNode.setRefreshContext(refreshContext);

    NodeConfig nodeConfig = new NodeConfig();
    taskNode.setNodeConfig(nodeConfig);
    return taskNode;
  }

  public PipelineDTO getPipelineDetail(String pipelineId) {
    List<PipelineNode> pipelineNodes = pipelineNodeService.list(
        Wrappers.lambdaQuery(PipelineNode.class).eq(PipelineNode::getPipelineId, pipelineId));
    Map<String, List<PipelineNodeDTO>> stageNodeMap = pipelineNodes.stream()
        .map(node -> OrikaUtil.convert(node, PipelineNodeDTO.class))
        .collect(Collectors.groupingBy(PipelineNodeDTO::getStageId));

    List<PipelineStage> pipelineStages = pipelineStageService.list(
        Wrappers.lambdaQuery(PipelineStage.class).eq(PipelineStage::getPipelineId, pipelineId)
            .orderByAsc(PipelineStage::getType));
    List<PipelineStageDTO> stageDTOList = pipelineStages.stream().map(stage -> {
      PipelineStageDTO stageDTO = OrikaUtil.convert(stage, PipelineStageDTO.class);
      stageDTO.setNodes(stageNodeMap.get(stage.getStageId()));
      return stageDTO;
    }).collect(Collectors.toList());

    PipelineDTO pipeline = getPipeline(pipelineId);
    pipeline.setStageList(stageDTOList);
    return pipeline;
  }

  public Boolean pause(String historyId) {
    List<NodeRecord> records = nodeRecordService.list(
        Wrappers.lambdaQuery(NodeRecord.class).eq(NodeRecord::getHistoryId, historyId)
            .eq(NodeRecord::getStatus, ProcessStatus.RUNNING.getType()));
    if (CollectionUtils.isEmpty(records)) {
      return false;
    }
    records.forEach(record -> {
      TaskNode taskNode = new TaskNode();
      taskNode.setRecordId(record.getRecordId());
      taskNode.setNodeId(record.getNodeId());
      taskNode.setHistoryId(historyId);
      taskNode.setNodeConfig(new NodeConfig());
      PipelineStatusEvent event = PipelineStatusEvent.builder().taskNode(taskNode)
          .processStatus(ProcessStatus.STOP).build();
      PipelineEventFactory.sendNotifyEvent(event);
    });

    return true;
  }
}
