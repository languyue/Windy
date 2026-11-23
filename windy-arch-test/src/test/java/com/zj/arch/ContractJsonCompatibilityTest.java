package com.zj.arch;

import com.zj.contract.enums.NotifyType;
import com.zj.contract.enums.ProcessStatus;
import com.zj.contract.event.ResultEvent;
import com.zj.contract.event.payload.NodeRecordPayload;
import com.zj.contract.feature.ExecutorUnit;
import com.zj.contract.json.ContractJson;
import com.zj.contract.json.WireJson;
import com.zj.contract.pipeline.ConfigDetail;
import com.zj.contract.pipeline.node.HttpContext;
import com.zj.contract.pipeline.node.MergeMasterContext;
import com.zj.contract.pipeline.node.RequestContext;
import com.zj.contract.pipeline.node.WaitContext;
import com.zj.contract.service.ApiParamModel;
import com.zj.plugin.loader.ParameterDefine;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DB 里已有的 JSON 列是 fastjson 写进去的，换成 {@link ContractJson}（Jackson）读写后，
 * 必须保持同一份数据能被原样读回来。这里把 repository 依赖的几条语义钉死：
 *
 * <ul>
 *   <li>空串/空白当作 null，写 null 得 null</li>
 *   <li>旧数据里多余的字段不能报错</li>
 *   <li>{@code boolean isXxx} 的属性名与 fastjson 一致（去掉 is 前缀）</li>
 *   <li>无类型 {@code Map<String, Object>} 里的小数按 BigDecimal 解析</li>
 *   <li>插件 SDK 的 {@code ParameterDefine} 能正常往返（含 Object 类型的 value）</li>
 * </ul>
 *
 * @author guyuelan
 */
public class ContractJsonCompatibilityTest {

  @Test
  void blankJsonShouldBeNull() {
    assertNull(ContractJson.read("", ConfigDetail.class));
    assertNull(ContractJson.read("   ", ConfigDetail.class));
    assertNull(ContractJson.readList(null, String.class));
    assertNull(ContractJson.write(null));
  }

  @Test
  void unknownFieldsFromOldDataShouldBeIgnored() {
    String oldData = "{\"actionId\":\"a1\",\"removedField\":\"legacy\",\"paramList\":{\"k\":\"v\"}}";
    ConfigDetail detail = ContractJson.read(oldData, ConfigDetail.class);
    assertNotNull(detail);
    assertEquals("a1", detail.getActionId());
    assertEquals("v", detail.getParamList().get("k"));
  }

  @Test
  void booleanGetterShouldKeepFastjsonPropertyName() {
    ApiParamModel param = new ApiParamModel();
    param.setParamKey("id");
    param.setRequired(true);
    String json = ContractJson.write(param);
    assertTrue(json.contains("\"required\":true"), "属性名必须与 fastjson 一致: " + json);
    ApiParamModel read = ContractJson.read(json, ApiParamModel.class);
    assertNotNull(read);
    assertTrue(read.isRequired());
  }

  @Test
  void untypedMapShouldUseBigDecimalLikeFastjson() {
    String json = "{\"ratio\":1.5,\"count\":3,\"name\":\"x\"}";
    Map<String, Object> map = ContractJson.readMap(json);
    assertNotNull(map);
    assertEquals(new BigDecimal("1.5"), map.get("ratio"));
    assertEquals(3, map.get("count"));
    assertEquals("x", map.get("name"));
  }

  @Test
  void executorUnitWithPluginParamShouldRoundTrip() {
    ParameterDefine define = new ParameterDefine();
    define.setParamKey("url");
    define.setType("String");
    define.setValue(Collections.singletonMap("nested", 1));

    ExecutorUnit unit = new ExecutorUnit();
    unit.setName("http");
    unit.setParams(Arrays.asList(define));

    String json = ContractJson.write(unit);
    ExecutorUnit read = ContractJson.read(json, ExecutorUnit.class);
    assertNotNull(read);
    assertEquals("http", read.getName());
    assertEquals(1, read.getParams().size());
    assertEquals("url", read.getParams().get(0).getParamKey());
    assertNotNull(read.getParams().get(0).getValue());
  }

  @Test
  void enumShouldBeWrittenAsName() {
    String json = ContractJson.write(ProcessStatus.SUCCESS);
    assertEquals("\"SUCCESS\"", json);
    assertEquals(ProcessStatus.SUCCESS, ContractJson.read("\"SUCCESS\"", ProcessStatus.class));
  }

  @Test
  void stringListShouldRoundTrip() {
    List<String> values = Arrays.asList("a", "b");
    String json = ContractJson.write(values);
    assertEquals(values, ContractJson.readList(json, String.class));
  }

  @Test
  void resultEventPayloadShouldBePolymorphic() {
    NodeRecordPayload payload = new NodeRecordPayload();
    payload.setRecordId("r1");
    payload.setNodeId("n1");
    payload.setResult(Arrays.asList("ok"));

    ResultEvent event = new ResultEvent().executeId("r1")
        .notifyType(NotifyType.CREATE_NODE_RECORD)
        .status(ProcessStatus.RUNNING)
        .params(payload);

    String json = WireJson.write(event);
    assertTrue(json.contains("\"notifyType\":\"CREATE_NODE_RECORD\""), json);

    ResultEvent read = ContractJson.read(json, ResultEvent.class);
    assertNotNull(read);
    assertNotNull(read.getParams());
    assertTrue(read.getParams() instanceof NodeRecordPayload,
        "payload 类型必须还原，实际=" + read.getParams().getClass());
    NodeRecordPayload readPayload = (NodeRecordPayload) read.getParams();
    assertEquals("r1", readPayload.getRecordId());
    assertEquals("n1", readPayload.getNodeId());
    assertEquals(1, readPayload.getResult().size());
  }

  @Test
  void resultEventWithoutPayloadShouldRoundTrip() {
    ResultEvent event = new ResultEvent().executeId("r2")
        .notifyType(NotifyType.UPDATE_NODE_RECORD)
        .status(ProcessStatus.SUCCESS);
    ResultEvent read = ContractJson.read(WireJson.write(event), ResultEvent.class);
    assertNotNull(read);
    assertNull(read.getParams());
  }

  @Test
  void requestContextShouldBePolymorphicByType() {
    HttpContext http = HttpContext.builder().url("http://a")
        .body(Collections.singletonMap("k", "v"))
        .bodyType("json").build();
    http.setPipelineId("p1");
    String json = WireJson.write(http);
    assertTrue(json.contains("\"type\":\"HTTP\""), json);

    RequestContext readHttp = ContractJson.read(json, RequestContext.class);
    assertTrue(readHttp instanceof HttpContext, "实际=" + readHttp.getClass());
    assertEquals("http://a", ((HttpContext) readHttp).getUrl());
    assertEquals("p1", readHttp.getPipelineId());

    WaitContext wait = new WaitContext();
    wait.setWaitTime(30);
    RequestContext readWait = ContractJson.read(WireJson.write(wait), RequestContext.class);
    assertTrue(readWait instanceof WaitContext, "实际=" + readWait.getClass());
    assertEquals(30, ((WaitContext) readWait).getWaitTime());

    MergeMasterContext merge = new MergeMasterContext();
    merge.setBranches(Arrays.asList("dev", "master"));
    merge.setDeleteBranch("1");
    merge.setMainBranch("master");
    RequestContext readMerge = ContractJson.read(WireJson.write(merge), RequestContext.class);
    assertTrue(readMerge instanceof MergeMasterContext, "实际=" + readMerge.getClass());
    assertEquals(2, ((MergeMasterContext) readMerge).getBranches().size());
    assertEquals("1", ((MergeMasterContext) readMerge).getDeleteBranch());
  }

  @Test
  void bareRequestContextAndUnknownTypeShouldFallBackToBase() {
    String bare = WireJson.write(new RequestContext());
    assertTrue(bare.contains("\"type\":\"NONE\""), bare);
    assertTrue(ContractJson.read(bare, RequestContext.class) instanceof RequestContext);

    RequestContext noType = ContractJson.read("{\"pipelineId\":\"p\"}", RequestContext.class);
    assertNotNull(noType);
    assertEquals("p", noType.getPipelineId());
  }
}
