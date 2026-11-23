package com.zj.master.dispatch.pipeline.builder;

import com.zj.common.enums.ExecuteType;
import com.zj.common.enums.Position;
import com.zj.contract.support.ParamValueCaster;
import com.zj.domain.entity.bo.pipeline.ActionParam;
import com.zj.domain.entity.bo.pipeline.PipelineActionBO;
import com.zj.master.entity.vo.ActionDetail;
import com.zj.contract.pipeline.node.ApprovalContext;
import com.zj.contract.pipeline.node.BuildCodeContext;
import com.zj.contract.pipeline.ConfigDetail;
import com.zj.contract.pipeline.node.DeployContext;
import com.zj.contract.pipeline.node.FeatureContext;
import com.zj.contract.pipeline.node.HttpContext;
import com.zj.contract.pipeline.node.MergeMasterContext;
import com.zj.contract.pipeline.node.RequestContext;
import com.zj.contract.pipeline.node.WaitContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/5/8
 */
@Slf4j
public class RequestContextBuilder {

    private static final Map<String, Function<ActionDetail, RequestContext>> factoryMap = new HashMap<>();

    static {
        factoryMap.put(ExecuteType.WAIT.name(), RequestContextBuilder::buildWaitContext);
        factoryMap.put(ExecuteType.HTTP.name(), RequestContextBuilder::buildHttpContext);
        factoryMap.put(ExecuteType.TEST.name(), RequestContextBuilder::buildTestContext);
        factoryMap.put(ExecuteType.DEPLOY.name(), RequestContextBuilder::buildDeployContext);
        factoryMap.put(ExecuteType.APPROVAL.name(), RequestContextBuilder::buildApprovalContext);
        factoryMap.put(ExecuteType.BUILD.name(), RequestContextBuilder::buildCodeContext);
        factoryMap.put(ExecuteType.MERGE.name(), RequestContextBuilder::buildMergeContext);
    }

    public static RequestContext createContext(ActionDetail actionDetail) {
        PipelineActionBO action = actionDetail.getAction();
        Function<ActionDetail, RequestContext> contextFunc = factoryMap.get(action.getExecuteType());
        if (Objects.isNull(contextFunc)) {
            log.info("can not find context function type={}", action.getExecuteType());
            return new RequestContext();
        }
        return contextFunc.apply(actionDetail);
    }

    private static RequestContext buildWaitContext(ActionDetail actionDetail) {
        WaitContext context = new WaitContext();
        Map<String, Object> paramList = getParamList(actionDetail);
        context.setWaitTime(getInteger(paramList, "waitTime"));
        return context;
    }

    private static RequestContext buildApprovalContext(ActionDetail actionDetail) {
        ApprovalContext context = new ApprovalContext();
        Map<String, Object> paramList = getParamList(actionDetail);
        context.setMaxWait(getInteger(paramList, "maxWait"));
        return context;
    }


    private static RequestContext buildHttpContext(ActionDetail actionDetail) {
        ConfigDetail configDetail = actionDetail.getConfigDetail();
        Map<String, Object> requestContext = configDetail.getParamList();
        String actionUrl = actionDetail.getAction().getActionUrl();
        Map<String, String> headers = new HashMap<>();
        Map<String, Object> body = new HashMap<>();
        if (CollectionUtils.isNotEmpty(actionDetail.getAction().getParamList())) {
            actionUrl = exchangeRequestUrl(actionDetail, requestContext, actionUrl);
            headers = toStringMap(getActionParamsByPosition(actionDetail, Position.Header.name(), requestContext));
            actionDetail.getAction().setHeaders(headers);
            actionUrl = warpPathParam(actionDetail, requestContext, actionUrl);
            body = getActionParamsByPosition(actionDetail, Position.Body.name(), requestContext);
        }
        return HttpContext.builder().body(body).bodyType(actionDetail.getAction().getBodyType())
                .url(actionUrl).headers(headers).build();
    }

    private static String warpPathParam(ActionDetail actionDetail, Map<String, Object> requestContext, String actionUrl) {
        Map<String, String> pathParams = toStringMap(
                getActionParamsByPosition(actionDetail, Position.Path.name(), requestContext));
        if (MapUtils.isEmpty(pathParams)) {
            return actionUrl;
        }
        StrSubstitutor strSubstitutor = new StrSubstitutor(pathParams);
        return strSubstitutor.replace(actionUrl);
    }

    private static String exchangeRequestUrl(ActionDetail actionDetail, Map<String, Object> requestContext,
            String actionUrl) {
        Map<String, String> queryParams = toStringMap(
                getActionParamsByPosition(actionDetail, Position.Query.name(), requestContext));
        return exchangeUrl(actionUrl, queryParams);
    }

    private static String exchangeUrl(String actionUrl, Map<String, String> queryParams) {
        List<String> queryList = queryParams.entrySet().stream().map(param -> param.getKey() + "=" + param.getValue())
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(queryList)) {
            return actionUrl;
        }

        String queryLine = String.join(",", queryList);
        if (!actionUrl.contains("?")){
            return actionUrl + "?" + queryLine;
        }
        return actionUrl + "&" + queryList;
    }

    /**
     * 参数值按声明的类型还原，后续链路只处理对象；Header/Path/Query 需要文本，单独收敛成字符串
     */
    private static Map<String, Object> getActionParamsByPosition(ActionDetail actionDetail, String position,
            Map<String, Object> requestContext) {
        Map<String, Object> params = new HashMap<>();
        actionDetail.getAction().getParamList().stream()
                .filter(param -> Objects.equals(position, param.getPosition()))
                .forEach(param -> {
                    Object value = ParamValueCaster.cast(requestContext.get(param.getName()), param.getType());
                    param.setValue(value);
                    params.put(param.getName(), value);
                });
        return params;
    }

    private static Map<String, String> toStringMap(Map<String, Object> params) {
        Map<String, String> textParams = new HashMap<>();
        params.forEach((name, value) -> textParams.put(name, Objects.isNull(value) ? null : String.valueOf(value)));
        return textParams;
    }

    private static RequestContext buildTestContext(ActionDetail actionDetail) {
        FeatureContext context = new FeatureContext();
        context.setTaskId(getString(getParamList(actionDetail), "taskId"));
        return context;
    }

    private static RequestContext buildDeployContext(ActionDetail actionDetail) {
        DeployContext context = new DeployContext();
        context.setEnvId(getString(getParamList(actionDetail), "envId"));
        return context;
    }

    private static RequestContext buildCodeContext(ActionDetail actionDetail) {
        BuildCodeContext context = new BuildCodeContext();
        Map<String, Object> paramList = getParamList(actionDetail);
        context.setPomPath(getString(paramList, "pomPath"));
        context.setBranches(getStringList(paramList, "branches"));
        return context;
    }

    private static RequestContext buildMergeContext(ActionDetail actionDetail) {
        MergeMasterContext context = new MergeMasterContext();
        Map<String, Object> paramList = getParamList(actionDetail);
        context.setTagName(getString(paramList, "tagName"));
        context.setMessage(getString(paramList, "message"));
        context.setBranches(getStringList(paramList, "branches"));
        context.setDeleteBranch(getString(paramList, "deleteBranch"));
        context.setMainBranch(getString(paramList, "mainBranch"));
        return context;
    }

    private static Map<String, Object> getParamList(ActionDetail actionDetail) {
        return Optional.ofNullable(actionDetail.getConfigDetail())
                .map(ConfigDetail::getParamList).orElseGet(HashMap::new);
    }

    /**
     * 空白值按 null 处理：历史上 fastjson 把空串转成 null，这里保持一致
     */
    private static String getString(Map<String, Object> paramList, String name) {
        Object value = paramList.get(name);
        if (Objects.isNull(value)) {
            return null;
        }
        String text = String.valueOf(value);
        return StringUtils.isBlank(text) ? null : text.trim();
    }

    private static Integer getInteger(Map<String, Object> paramList, String name) {
        String value = getString(paramList, name);
        return Objects.isNull(value) ? null : Integer.valueOf(value);
    }

    /**
     * 历史上 fastjson 会把 "a,b" 这类字符串拆成列表，这里保持一致
     */
    private static List<String> getStringList(Map<String, Object> paramList, String name) {
        String value = getString(paramList, name);
        return Objects.isNull(value) ? null : Arrays.asList(value.split(","));
    }
}
