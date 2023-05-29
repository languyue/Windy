package com.zj.domain.entity.dto.pipeline;

import java.util.List;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author guyuelan
 * @since 2021/9/28
 */
@Data
public class PipelineDto {

  /**
   * 流水线Id
   */
  private String pipelineId;

  /**
   * 流水线名称
   */
  @NotEmpty(message = "流水线名称不能为空")
  private String pipelineName;

  /**
   * 服务Id
   */
  @NotEmpty(message = "服务Id不能为空")
  private String serviceId;

  /**
   * 服务名
   */
  private String ServiceName;

  /**
   * 流水线创建者
   */
  private String creator;

  /**
   * 流水线类型
   */
  @NotNull(message = "流水线类型不能为空")
  @Min(1)
  @Max(3)
  private Integer pipelineType;

  /**
   * 执行方式
   * */
  @NotNull(message = "执行方式不能为空")
  @Min(1)
  @Max(3)
  private Integer executeType;

  /**
   * 流水线配置
   */
  private String pipelineConfig;

  /**
   * 流水线状态
   */
  private Integer pipelineStatus;

  @NotEmpty
  private List<PipelineStageDto> stageList;

  public PipelineDto() {
  }
}
