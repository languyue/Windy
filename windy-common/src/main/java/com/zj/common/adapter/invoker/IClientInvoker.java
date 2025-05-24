package com.zj.common.adapter.invoker;

import com.zj.common.entity.dto.ClientCollectDto;
import com.zj.common.entity.dto.StopDispatch;
import com.zj.common.entity.service.ToolLoadResult;
import com.zj.common.entity.service.ToolVersionDto;

import java.util.List;

public interface IClientInvoker {

    /**
     * 触发执行API二方包执行
     * @param generateInfo 生产信息
     * @return 是否触发成功
     */
    boolean runGenerateTask(Object generateInfo);

    /**
     * 执行流水线任务
     * @param pipelineTask 流水线任务信息
     * @param isRequestSingle 是否继续固定单节点执行
     * @param singleIp client节点IP
     * @return 触发流水线是否成功
     */
    boolean runPipelineTask(Object pipelineTask, boolean isRequestSingle, String singleIp);

    /**
     * 执行用例任务
     * @param featureTask 用例任务信息
     * @return 用例是否触发成功
     */
    boolean runFeatureTask(Object featureTask);

    /**
     * 停止任务状态轮训
     * @param stopDispatch 停止的任务信息
     */
    void stopTaskLoopQuery(StopDispatch stopDispatch);

    /**
     * 获取client节点监控
     * @return client节点监控信息
     */
    List<ClientCollectDto> requestClientMonitor();

    /**
     * 校验配置的工具是否在所有client节点安装，在Windy的全局配置中使用
     * @param toolVersion 工具版本信息
     * @return 返回工具测试安装结果
     */
    List<ToolLoadResult> loadBuildTool(ToolVersionDto toolVersion);
}
