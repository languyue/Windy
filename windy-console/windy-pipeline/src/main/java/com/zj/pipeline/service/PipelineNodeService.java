package com.zj.pipeline.service;

import com.zj.domain.entity.bo.pipeline.PipelineNodeBO;
import com.zj.domain.repository.pipeline.IPipelineNodeCommandRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @author guyuelan
 * @since 2023/3/13
 */
@Service
public class PipelineNodeService {
  private final IPipelineNodeCommandRepository pipelineNodeRepository;

  public PipelineNodeService(IPipelineNodeCommandRepository pipelineNodeRepository) {
    this.pipelineNodeRepository = pipelineNodeRepository;
  }


  public boolean deleteNodeIds(List<String> nodeIds) {
    return pipelineNodeRepository.deleteNodeIds(nodeIds);
  }

  public void updateNode(PipelineNodeBO dto) {
    pipelineNodeRepository.updateNode(dto);
  }

  public boolean deleteByPipeline(String pipelineId) {
    return pipelineNodeRepository.deleteByPipelineId(pipelineId);
  }

  public void saveNode(PipelineNodeBO pipelineNode) {
    pipelineNodeRepository.saveNode(pipelineNode);
  }

  public List<PipelineNodeBO> getPipelineNodes(String pipelineId) {
    return pipelineNodeRepository.getPipelineNodes(pipelineId);
  }
}
