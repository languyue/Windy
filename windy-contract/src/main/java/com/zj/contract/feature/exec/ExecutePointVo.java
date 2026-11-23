package com.zj.contract.feature.exec;

import com.zj.contract.feature.CompareDefine;
import com.zj.contract.feature.ExecutorUnit;
import com.zj.contract.feature.VariableDefine;
import lombok.Data;

import java.util.List;

/**
 * 用例执行点，master -> client 下发的可执行子集。
 *
 * <p>只保留执行任务真正需要的字段，不含主键与审计时间，断开对外契约对存储层的依赖。</p>
 *
 * @author guyuelan
 */
@Data
public class ExecutePointVo {

  /**
   * 执行点Id
   * */
  private String pointId;

  /**
   * 执行类型
   * */
  private Integer executeType;

  /**
   * 用例Id
   * */
  private String featureId;

  /**
   * 执行点信息(包含参数信息)
   * */
  private ExecutorUnit executorUnit;

  /**
   * 包含比较信息
   * */
  private List<CompareDefine> compareDefines;

  /**
   * 包含变量声明
   * */
  private List<VariableDefine> variableDefines;

  /**
   * 执行点功能描述，自定义
   * */
  private String description;

  /**
   * 测试阶段
   * */
  private Integer testStage;

  /**
   * 执行顺序
   * */
  private Integer sortOrder;
}
