package com.zj.common.entity.feature;

import com.zj.common.enums.ProcessStatus;
import com.zj.plugin.loader.ExecuteDetailVo;
import lombok.Data;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;


@Data
public class FeatureResponse {
    /**
     * 执行的详细信息
     */
    private ExecuteDetailVo executeDetailVo;

    private CompareResult compareResult;

    /**
     * 执行过程中需要设置的临时全局变量
     */
    private Map<String, Object> context;

    /**
     * 模版名称
     */
    private String name;

    /**
     * 执行点Id
     */
    private String pointId;

    /**
     * 执行状态
     */
    private Integer status;

    /**
     * 用例执行花费时间
     */
    private Long spendTime;

    public boolean isSuccess() {
        boolean invokeStatus = true;
        if (Objects.nonNull(executeDetailVo)) {
            invokeStatus = Optional.ofNullable(executeDetailVo.responseStatus()).orElse(false);
        }

        boolean compareStatus = true;
        if (Objects.nonNull(compareResult)){
            compareStatus = compareResult.isCompareSuccess();
        }

        return invokeStatus && compareStatus;
    }

    public boolean isProcessing(){
        if (Objects.isNull(executeDetailVo) || Objects.isNull(executeDetailVo.getResponseDetailVo())) {
            return false;
        }
        return Objects.equals(executeDetailVo.getResponseDetailVo().getProcessStatus(),
                ProcessStatus.RUNNING.getType());
    }

    public static FeatureResponse builder(){
        return new FeatureResponse();
    }

    public FeatureResponse executeDetailVo(ExecuteDetailVo executeDetailVo){
        this.executeDetailVo = executeDetailVo;
        return this;
    }

    public FeatureResponse compareResult(CompareResult compareResult){
        this.compareResult = compareResult;
        return this;
    }

    public FeatureResponse context(Map<String, Object>  context){
        this.context = context;
        return this;
    }

    public FeatureResponse name(String  name){
        this.name = name;
        return this;
    }

    public FeatureResponse pointId(String  pointId){
        this.pointId = pointId;
        return this;
    }
    public FeatureResponse spendTime(Long  spendTime){
        this.spendTime = spendTime;
        return this;
    }

    public FeatureResponse build() {
        return this;
    }

    public Integer getExecuteStatus() {
        if (isProcessing()){
            return ProcessStatus.RUNNING.getType();
        }
        return isSuccess() ? ProcessStatus.SUCCESS.getType(): ProcessStatus.FAIL.getType();
    }
}
