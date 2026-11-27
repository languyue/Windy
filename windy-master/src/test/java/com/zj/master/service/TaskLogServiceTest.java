package com.zj.master.service;

import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.contract.dispatch.DispatchResult;
import com.zj.contract.dispatch.DispatchTaskModel;
import com.zj.contract.dispatch.FeatureDispatchResult;
import com.zj.contract.dispatch.GenerateDispatchResult;
import com.zj.contract.dispatch.PipelineDispatchResult;
import com.zj.contract.enums.DispatchStatus;
import com.zj.contract.enums.LogType;
import com.zj.contract.enums.ProcessStatus;
import com.zj.domain.entity.bo.log.DispatchLogBO;
import com.zj.domain.repository.log.IDispatchLogCommandRepository;
import com.zj.master.dispatch.Dispatcher;
import com.zj.master.dispatch.IDispatchExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 调度入口的类型归属口径回归：类型在入口流程里定，拒绝在选 executor 之前拦住。
 *
 * <ul>
 *   <li>feature 入口认两种 flow，其余类型必须直接拒绝，不能先落库、不能先进 executor</li>
 *   <li>pipeline/generate 的类型由入口决定，请求体里传错也要被覆盖成正确类型再下发</li>
 *   <li>executor 返回 rejected 时日志必须收尾成 FAIL，不能停在 RUNNING</li>
 * </ul>
 *
 * @author guyuelan
 */
public class TaskLogServiceTest {

  private RecordingLogRepository repository;
  private Map<LogType, RecordingExecutor> executors;
  private TaskLogService taskLogService;

  @BeforeEach
  void setUp() {
    repository = new RecordingLogRepository();
    executors = new LinkedHashMap<>();
    registerExecutor(LogType.FEATURE_TASK, FeatureDispatchResult.accepted("record-1"));
    registerExecutor(LogType.PIPELINE, PipelineDispatchResult.accepted("history-1"));
    registerExecutor(LogType.GENERATE, GenerateDispatchResult.accepted("生成任务已下发"));

    Dispatcher dispatcher = new Dispatcher(new ArrayList<>(executors.values()));
    taskLogService = new TaskLogService(new FixedIdService(), repository, dispatcher,
        Collections.emptyList());
  }

  /**
   * 请求体带 PIPELINE 打到 feature 入口时，校验必须发生在 dispatch 与落库之前，
   * 否则流水线会被真的拉起来，再返回 rejected 也收不回副作用
   */
  @Test
  void createFeatureTaskWithNonFeatureTypeShouldRejectBeforeDispatch() {
    FeatureDispatchResult result = taskLogService.createFeatureTask(
        new DispatchTaskModel("s1", "source", LogType.PIPELINE.getType()));

    assertFalse(result.accepted());
    assertTrue(result.getMessage().contains("FEATURE"), result.getMessage());
    assertTrue(repository.savedLogs.isEmpty(), "类型不合法时不能落调度日志");
    assertTrue(repository.statusUpdates.isEmpty(), "类型不合法时没有日志需要收尾");
    executors.values().forEach(executor -> assertTrue(executor.dispatchedTasks.isEmpty(),
        "类型不合法时不能进入 executor: " + executor.type()));
  }

  /**
   * feature 入口的两个合法类型仍要正常下发，并把 recordId 透传给调用方
   */
  @Test
  void createFeatureTaskWithFeatureTaskTypeShouldDispatch() {
    FeatureDispatchResult result = taskLogService.createFeatureTask(
        new DispatchTaskModel("f1", "feature", LogType.FEATURE_TASK.getType()));

    assertTrue(result.accepted());
    assertEquals("record-1", result.getRecordId());
    assertEquals(1, executors.get(LogType.FEATURE_TASK).dispatchedTasks.size());
  }

  /**
   * 请求体自带 GENERATE 打到 pipeline 入口时，type 必须被覆盖成 PIPELINE：
   * 落库的日志类型与交给 executor 的任务类型都要是 PIPELINE
   */
  @Test
  void createPipelineTaskShouldOverrideWrongType() {
    DispatchTaskModel task = new DispatchTaskModel("p1", "pipeline", LogType.GENERATE.getType());

    PipelineDispatchResult result = taskLogService.createPipelineTask(task);

    assertTrue(result.accepted());
    assertEquals("history-1", result.getHistoryId());
    assertEquals(1, repository.savedLogs.size());
    DispatchLogBO savedLog = repository.savedLogs.get(0);
    assertEquals("log-1", savedLog.getLogId());
    assertEquals(LogType.PIPELINE.getType(), savedLog.getLogType());
    assertEquals(ProcessStatus.RUNNING.getType(), savedLog.getLogStatus());

    List<DispatchTaskModel> dispatched = executors.get(LogType.PIPELINE).dispatchedTasks;
    assertEquals(1, dispatched.size());
    assertEquals(LogType.PIPELINE.getType(), dispatched.get(0).getType());
    assertTrue(repository.statusUpdates.isEmpty(), "受理的任务不能被标记失败");
  }

  /**
   * 请求体自带 FEATURE 打到 generate 入口时，type 必须被覆盖成 GENERATE
   */
  @Test
  void createGenerateTaskShouldOverrideWrongType() {
    DispatchTaskModel task = new DispatchTaskModel("g1", "generate", LogType.FEATURE.getType());

    GenerateDispatchResult result = taskLogService.createGenerateTask(task);

    assertTrue(result.accepted());
    assertEquals(1, repository.savedLogs.size());
    assertEquals(LogType.GENERATE.getType(), repository.savedLogs.get(0).getLogType());

    List<DispatchTaskModel> dispatched = executors.get(LogType.GENERATE).dispatchedTasks;
    assertEquals(1, dispatched.size());
    assertEquals(LogType.GENERATE.getType(), dispatched.get(0).getType());
  }

  /**
   * executor 拒绝后不会再有回调收尾，日志必须在入口直接置为 FAIL
   */
  @Test
  void dispatchRejectedShouldMarkLogFail() {
    executors.get(LogType.PIPELINE).result = PipelineDispatchResult.rejected("找不到流水线");

    PipelineDispatchResult result = taskLogService.createPipelineTask(
        new DispatchTaskModel("p1", "pipeline", LogType.PIPELINE.getType()));

    assertFalse(result.accepted());
    assertEquals("找不到流水线", result.getMessage());
    assertEquals(1, repository.statusUpdates.size());
    assertEquals("log-1", repository.statusUpdates.get(0).logId);
    assertEquals(ProcessStatus.FAIL.getType(), repository.statusUpdates.get(0).status);
  }

  /**
   * 没有 executor 时 Dispatcher 返回基类结果，入口要把它收窄成具体类型并保留拒绝原因
   */
  @Test
  void baseRejectedResultShouldFallBackToTypedResult() {
    executors.get(LogType.GENERATE).result =
        new DispatchResult(DispatchStatus.REJECTED, "不支持的调度任务类型");

    GenerateDispatchResult result = taskLogService.createGenerateTask(
        new DispatchTaskModel("g1", "generate", LogType.GENERATE.getType()));

    assertFalse(result.accepted());
    assertEquals("不支持的调度任务类型", result.getMessage());
    assertEquals(1, repository.statusUpdates.size());
    assertEquals(ProcessStatus.FAIL.getType(), repository.statusUpdates.get(0).status);
  }

  private void registerExecutor(LogType logType, DispatchResult result) {
    RecordingExecutor executor = new RecordingExecutor(logType);
    executor.result = result;
    executors.put(logType, executor);
  }

  /**
   * 记录下发到每个 executor 的任务，用来断言"没进 executor"与"类型已被覆盖"
   */
  private static class RecordingExecutor implements IDispatchExecutor {

    private final LogType logType;
    private final List<DispatchTaskModel> dispatchedTasks = new ArrayList<>();
    private DispatchResult result;

    private RecordingExecutor(LogType logType) {
      this.logType = logType;
    }

    @Override
    public LogType type() {
      return logType;
    }

    @Override
    public boolean isExistInJvm(DispatchLogBO taskLog) {
      return false;
    }

    @Override
    public DispatchResult dispatch(DispatchTaskModel task, String logId) {
      dispatchedTasks.add(task);
      return result;
    }

    @Override
    public boolean resume(DispatchLogBO taskLog) {
      return false;
    }

    @Override
    public Integer getExecuteCount() {
      return dispatchedTasks.size();
    }
  }

  /**
   * 记录日志落库与状态更新，未覆盖的方法在单测里用不到
   */
  private static class RecordingLogRepository implements IDispatchLogCommandRepository {

    private final List<DispatchLogBO> savedLogs = new ArrayList<>();
    private final List<StatusUpdate> statusUpdates = new ArrayList<>();

    @Override
    public List<String> delete7DayLog() {
      return Collections.emptyList();
    }

    @Override
    public void saveLog(DispatchLogBO taskLog) {
      savedLogs.add(taskLog);
    }

    @Override
    public boolean updateLogStatus(String logId, int type) {
      statusUpdates.add(new StatusUpdate(logId, type));
      return true;
    }

    @Override
    public boolean updateLogMasterIp(String logId, String localIP, Integer lockVersion) {
      return true;
    }

    @Override
    public boolean updateLogSourceRecord(String logId, String recordId) {
      return true;
    }

    @Override
    public List<DispatchLogBO> getRunningDispatchLog() {
      return Collections.emptyList();
    }

    @Override
    public DispatchLogBO getDispatchLog(String logId) {
      return null;
    }
  }

  private static class StatusUpdate {

    private final String logId;
    private final int status;

    private StatusUpdate(String logId, int status) {
      this.logId = logId;
      this.status = status;
    }
  }

  /**
   * 固定日志Id，保证状态更新的断言可定位
   */
  private static class FixedIdService extends UniqueIdService {

    @Override
    public String getUniqueId() {
      return "log-1";
    }
  }
}
