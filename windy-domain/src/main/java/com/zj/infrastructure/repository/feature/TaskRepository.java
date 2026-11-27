package com.zj.infrastructure.repository.feature;

import com.zj.contract.rpc.PageSize;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.contract.feature.exec.BatchFeatureVo;
import com.zj.contract.json.ContractJson;
import com.zj.domain.entity.bo.feature.TaskInfoBO;
import com.zj.infrastructure.entity.po.feature.TaskInfo;
import com.zj.infrastructure.mapper.feature.TaskInfoMapper;
import com.zj.domain.repository.feature.ITaskCommandRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Repository
public class TaskRepository extends ServiceImpl<TaskInfoMapper, TaskInfo> implements
    ITaskCommandRepository {

  @Override
  public PageSize<TaskInfoBO> getFuzzTaskList(String name, Integer pageNum, Integer size, String userId, String serviceId) {
    LambdaQueryWrapper<TaskInfo> wrapper =
            Wrappers.lambdaQuery(TaskInfo.class).orderByDesc(TaskInfo::getCreateTime);
    if (StringUtils.isNotBlank(name)) {
      wrapper = wrapper.like(TaskInfo::getTaskName, name);
    }
    if (StringUtils.isNotBlank(serviceId)) {
      wrapper.eq(TaskInfo::getServiceId, serviceId);
    }else {
      wrapper.eq(TaskInfo::getUserId, userId);
    }
    IPage<TaskInfo> page = new Page<>(pageNum, size);
    IPage<TaskInfo> taskPage = page(page, wrapper);

    PageSize<TaskInfoBO> result = new PageSize<>();
    result.setTotal(taskPage.getTotal());
    result.setData(taskPage.getRecords().stream().map(this::convertTaskInfoBO)
        .collect(Collectors.toList()));
    return result;
  }

  @Override
  public boolean createTask(TaskInfoBO taskInfoBO) {
    TaskInfo taskInfo = convertTaskInfo(taskInfoBO);
    taskInfo.setCreateTime(System.currentTimeMillis());
    taskInfo.setUpdateTime(System.currentTimeMillis());
    return save(taskInfo);
  }

  @Override
  public boolean updateTask(TaskInfoBO taskInfoBO) {
    TaskInfo taskInfo = convertTaskInfo(taskInfoBO);
    taskInfo.setUpdateTime(System.currentTimeMillis());

    return update(taskInfo,
        Wrappers.lambdaUpdate(TaskInfo.class).eq(TaskInfo::getTaskId, taskInfoBO.getTaskId()));
  }

  @Override
  public boolean deleteTask(String taskId) {
    return remove(Wrappers.lambdaQuery(TaskInfo.class).eq(TaskInfo::getTaskId, taskId));
  }

  @Override
  public TaskInfoBO getTaskDetail(String taskId) {
    TaskInfo taskInfo = getOne(
        Wrappers.lambdaQuery(TaskInfo.class).eq(TaskInfo::getTaskId, taskId));
    return convertTaskInfoBO(taskInfo);
  }

  @Override
  public List<TaskInfoBO> getAllTaskList(String serviceId) {
    List<TaskInfo> taskInfos = list(
        Wrappers.lambdaQuery(TaskInfo.class).eq(TaskInfo::getServiceId, serviceId));
    return taskInfos.stream().map(this::convertTaskInfoBO).collect(Collectors.toList());
  }

  private TaskInfo convertTaskInfo(TaskInfoBO taskInfoBO) {
    TaskInfo taskInfo = OrikaUtil.convert(taskInfoBO, TaskInfo.class);
    taskInfo.setTaskConfig(ContractJson.write(taskInfoBO.getExecuteConfig()));
    return taskInfo;
  }

  private TaskInfoBO convertTaskInfoBO(TaskInfo taskInfo) {
    TaskInfoBO taskInfoBO = OrikaUtil.convert(taskInfo, TaskInfoBO.class);
    taskInfoBO.setExecuteConfig(ContractJson.read(taskInfo.getTaskConfig(), BatchFeatureVo.class));
    return taskInfoBO;
  }
}
