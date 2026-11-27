package com.zj.infrastructure.repository.pipeline;

import com.zj.contract.json.ContractJson;
import com.fasterxml.jackson.core.type.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.contract.rpc.PageSize;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.pipeline.ActionParam;
import com.zj.contract.pipeline.CompareParameter;
import com.zj.domain.entity.bo.pipeline.PipelineActionBO;
import com.zj.infrastructure.entity.po.pipeline.PipelineAction;
import com.zj.infrastructure.mapper.pipeline.PipelineActionMapper;
import com.zj.domain.repository.pipeline.IPipelineActionCommandRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
@Repository
public class PipelineActionRepository extends
        ServiceImpl<PipelineActionMapper, PipelineAction> implements IPipelineActionCommandRepository {

    @Override
    public Boolean createAction(PipelineActionBO actionDto) {
        PipelineAction pipelineAction = OrikaUtil.convert(actionDto, PipelineAction.class);
        long dateNow = System.currentTimeMillis();
        pipelineAction.setCreateTime(dateNow);
        pipelineAction.setUpdateTime(dateNow);
        pipelineAction.setHeaders(ContractJson.write(actionDto.getHeaders()));
        pipelineAction.setResult(ContractJson.write(actionDto.getCompareResults()));
        pipelineAction.setParamDetail(ContractJson.write(actionDto.getParamList()));
        return save(pipelineAction);
    }

    @Override
    public PipelineActionBO getAction(String actionId) {
        PipelineAction action = getOne(
                Wrappers.lambdaQuery(PipelineAction.class).eq(PipelineAction::getActionId, actionId));
        return Optional.ofNullable(action).map(this::convertActionBO).orElse(null);
    }

    @Override
    public Boolean updateAction(PipelineActionBO actionDto) {
        PipelineAction pipelineAction = convertPipelineAction(actionDto);
        return update(pipelineAction, Wrappers.lambdaUpdate(PipelineAction.class)
                .eq(PipelineAction::getActionId, actionDto.getActionId()));
    }

    @Override
    public Boolean actionsBindNode(String nodeId, List<String> actionIds) {
        PipelineAction pipelineAction = new PipelineAction();
        pipelineAction.setNodeId(nodeId);
        return update(pipelineAction,
                Wrappers.lambdaUpdate(PipelineAction.class).in(PipelineAction::getActionId, actionIds));
    }

    @Override
    public Boolean deleteAction(String actionId) {
        return remove(
                Wrappers.lambdaQuery(PipelineAction.class).eq(PipelineAction::getActionId, actionId));
    }

    @Override
    public PageSize<PipelineActionBO> getActions(Integer page, Integer size, String name) {
        LambdaQueryWrapper<PipelineAction> queryWrapper = Wrappers.lambdaQuery(PipelineAction.class)
                .orderByDesc(PipelineAction::getCreateTime);
        if (!StringUtils.isEmpty(name)) {
            queryWrapper.like(PipelineAction::getActionName, name);
        }
        IPage<PipelineAction> actionIPage = new Page<>(page, size);
        IPage<PipelineAction> queryPage = page(actionIPage, queryWrapper);
        PageSize<PipelineActionBO> pageSize = new PageSize<>();
        pageSize.setTotal(queryPage.getTotal());
        pageSize.setData(queryPage.getRecords().stream().map(this::convertActionBO).collect(Collectors.toList()));
        return pageSize;
    }

    @Override
    public List<PipelineActionBO> getActionsByNodeId(String nodeId) {
        List<PipelineAction> actions = list(Wrappers.lambdaQuery(PipelineAction.class).eq(PipelineAction::getNodeId, nodeId));
        return Optional.ofNullable(actions).map(actionList -> actionList.stream().map(this::convertActionBO)
                .collect(Collectors.toList())).orElseGet(ArrayList::new);
    }

    @Override
    public boolean batchDelete(List<String> actionIds) {
        if (CollectionUtils.isEmpty(actionIds)) {
            return false;
        }
        return remove(Wrappers.lambdaQuery(PipelineAction.class).in(PipelineAction::getActionId, actionIds));
    }

    private PipelineActionBO convertActionBO(PipelineAction action) {
        PipelineActionBO actionDto = OrikaUtil.convert(action, PipelineActionBO.class);
        actionDto.setHeaders(ContractJson.read(action.getHeaders(), new TypeReference<Map<String, String>>() {
        }));
        actionDto.setCompareResults(ContractJson.readList(action.getResult(), CompareParameter.class));
        actionDto.setParamList(ContractJson.readList(action.getParamDetail(), ActionParam.class));
        actionDto.setLoopExpression(ContractJson.read(action.getQueryExpression(), CompareParameter.class));
        return actionDto;
    }
    private static PipelineAction convertPipelineAction(PipelineActionBO actionDto) {
        PipelineAction pipelineAction = OrikaUtil.convert(actionDto, PipelineAction.class);
        Optional.ofNullable(actionDto.getHeaders()).ifPresent(headers -> pipelineAction.setHeaders(ContractJson.write(headers)));
        Optional.ofNullable(actionDto.getLoopExpression()).ifPresent(loopExpression -> pipelineAction.setQueryExpression(ContractJson.write(loopExpression)));
        Optional.ofNullable(actionDto.getParamList()).ifPresent(params -> pipelineAction.setParamDetail(ContractJson.write(params)));
        Optional.ofNullable(actionDto.getCompareResults()).ifPresent(compareResults -> pipelineAction.setResult(ContractJson.write(compareResults)));
        return pipelineAction;
    }
}
